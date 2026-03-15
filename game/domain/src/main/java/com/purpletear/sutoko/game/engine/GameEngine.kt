package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core game engine that manages node execution and state.
 * State survives process death via GameSessionStorage (provided by presentation layer).
 * Game memory is persisted to database at explicit save points.
 * 
 * Timing orchestration:
 * - Message nodes: seenMs delay -> typing -> waitMs delay -> message -> next
 * - Other nodes: immediate execution via handlers
 * 
 * NOTE: This class focuses on ORCHESTRATION only. All node-specific processing
 * is delegated to handlers or the TextProcessor.
 */
@Singleton
class GameEngine @Inject constructor(
    private val handlerFactory: NodeHandlerFactory,
    private val nodeResolver: NodeResolver,
    private val timingScheduler: TimingScheduler,
    private val textProcessor: TextProcessor,
    private val memory: GameMemory
) {

    private val _state = MutableStateFlow<GameEngineState>(GameEngineState.Idle)
    val state: StateFlow<GameEngineState> = _state.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events: Flow<GameEvent> = _events.receiveAsFlow()

    private var currentGraph: ChapterGraph? = null
    private var currentGameId: String? = null
    private var isPaused = false
    private var isFastMode: Boolean = false

    /**
     * Initializes the engine for a game session.
     * Loads persisted memory from database.
     * 
     * @param gameId Unique identifier for the game
     * @param graph The chapter graph to execute
     * @param fastMode Skip timing delays (for testing/debug)
     */
    suspend fun initialize(gameId: String, graph: ChapterGraph, fastMode: Boolean = false) {
        currentGameId = gameId
        currentGraph = graph
        isFastMode = fastMode
        
        // Load persisted memory from database
        memory.load(gameId)
        
        _state.value = GameEngineState.Ready(
            chapterCode = graph.chapterCode,
            currentNodeId = graph.startNodeId
        )
    }

    suspend fun start() {
        val graph = currentGraph ?: return
        val currentNodeId = graph.startNodeId
        
        _state.value = GameEngineState.Playing(
            chapterCode = graph.chapterCode,
            currentNodeId = currentNodeId,
            messages = emptyList()
        )

        executeNode(currentNodeId)
    }

    suspend fun resume() {
        isPaused = false
    }

    /**
     * Pauses the engine and persists memory to database.
     * Should be called when app goes to background.
     */
    suspend fun pause() {
        isPaused = true
        saveMemory()
    }

    /**
     * Main entry point for executing a node.
     * Dispatches to specific handlers based on node type.
     */
    private suspend fun executeNode(nodeId: String) {
        if (isPaused) return
        assert(currentGraph != null)

        val graph = currentGraph ?: return

        val node = graph.getNode(nodeId)

        if (node == null) {
            _state.value = GameEngineState.Error("Node not found: $nodeId")
            return
        }

        updateCurrentNodeId(nodeId)

        when (node) {
            is Node.Message -> executeMessageNode(graph, node)
            else -> executeImmediateNode(graph, node)
        }
    }

    /**
     * Executes a message node with timing orchestration:
     * 1. seenMs delay (optional) - wait before showing typing
     * 2. Show typing indicator
     * 3. waitMs delay (optional) - typing duration
     * 4. Show message
     * 5. Navigate to next node
     */
    private suspend fun executeMessageNode(graph: ChapterGraph, node: Node.Message) {
        // 1. SEEN DELAY: Wait before showing typing (simulates "reading" previous message)
        if (node.seenMs > 0 && !isFastMode) {
            timingScheduler.delay(node.seenMs)
        }

        if (isPaused) return

        // 2. TYPING DELAY: Show typing indicator
        if (node.waitMs > 0 && !isFastMode) {
            emit(GameEvent.ShowTypingIndicator(node.characterId))
            timingScheduler.delay(node.waitMs)
        }

        if (isPaused) return

        // 3. PROCESS TEXT and SHOW MESSAGE
        val variables = memory.state.value
        val processedText = textProcessor.process(node.text, variables)
        
        emit(
            GameEvent.ShowMessage(
                text = processedText,
                characterId = node.characterId,
                isMainCharacter = node.characterId == 0
            )
        )

        // 4. NAVIGATE (memory auto-saved at chapter end/pause, not here)
        navigateToNext(graph, node, null)
    }

    /**
     * Executes nodes that have no timing requirements.
     * Uses handlers for logic, then immediately navigates.
     */
    private suspend fun executeImmediateNode(graph: ChapterGraph, node: Node) {
        val handler = getHandler(node)

        val handlerResult = try {
            handler.handle(node, memory) { event ->
                _events.trySend(event)
            }
        } catch (e: IllegalStateException) {
            _state.value = GameEngineState.Error("State error: ${e.message}")
            return
        } catch (e: IllegalArgumentException) {
            _state.value = GameEngineState.Error("Invalid argument: ${e.message}")
            return
        } catch (e: IndexOutOfBoundsException) {
            _state.value = GameEngineState.Error("Index out of bounds: ${e.message}")
            return
        }

        navigateToNext(graph, node, handlerResult)
    }

    private suspend fun navigateToNext(
        graph: ChapterGraph,
        currentNode: Node,
        handlerResult: String?
    ) {
        when (val result = nodeResolver.resolveNextNode(graph, currentNode, handlerResult)) {
            is NodeResolver.ResolutionResult.NextNode -> {
                executeNode(result.nodeId)
            }
            is NodeResolver.ResolutionResult.ChapterComplete -> {
                saveMemory()  // Persist at chapter end
                _state.value = GameEngineState.Completed(graph.chapterCode)
            }
            is NodeResolver.ResolutionResult.Error -> {
                _state.value = GameEngineState.Error(result.message)
            }

            else -> {

            }
        }
    }

    private fun updateCurrentNodeId(nodeId: String) {
        val currentState = state.value
        if (currentState is GameEngineState.Playing) {
            _state.value = currentState.copy(currentNodeId = nodeId)
        }
    }

    private fun getHandler(node: Node): NodeHandler {
        val type = when (node) {
            is Node.Start -> NodeType.START
            is Node.Message -> NodeType.MESSAGE
            is Node.ChapterChange -> NodeType.CHAPTER_CHANGE
            is Node.Condition -> NodeType.CONDITION
            is Node.Memory -> NodeType.MEMORY
            is Node.Info -> NodeType.INFO
            is Node.Trophy -> NodeType.TROPHY
            is Node.Signal -> NodeType.SIGNAL
            is Node.Background -> NodeType.BACKGROUND
        }
        return handlerFactory.getHandler(type)
    }

    /**
     * Persists current memory state to database.
     * Called at explicit save points: pause and chapter complete.
     */
    private suspend fun saveMemory() {
        memory.save()
    }

    private fun emit(event: GameEvent) {
        _events.trySend(event)
    }
}
