package com.purpletear.sutoko.game.engine

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

/**
 * Core game engine that manages node execution and state.
 * State survives process death via StatePersistence (provided by presentation layer).
 * 
 * Timing orchestration:
 * - Message nodes: seenMs delay -> typing -> waitMs delay -> message -> next
 * - Choice nodes: wait for user input
 * - Other nodes: immediate execution
 */
class GameEngine @Inject constructor(
    private val handlerFactory: NodeHandlerFactory,
    private val navigator: NodeNavigator,
    private val statePersistence: StatePersistence,
    private val timingScheduler: TimingScheduler
) {
    companion object {
        private const val KEY_CURRENT_NODE_ID = "current_node_id"
        private const val KEY_CHAPTER_CODE = "chapter_code"
        private const val KEY_MEMORY_PREFIX = "memory_"
        private const val KEY_FAST_MODE = "fast_mode"
    }

    private val _state = MutableStateFlow<GameEngineState>(GameEngineState.Idle)
    val state: StateFlow<GameEngineState> = _state.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events: Flow<GameEvent> = _events.receiveAsFlow()

    val memory = GameMemory()
    private var currentGraph: ChapterGraph? = null
    private var isPaused = false
    private var isFastMode: Boolean = false

    init {
        restoreMemory()
        isFastMode = statePersistence.getString(KEY_FAST_MODE)?.toBoolean() ?: false
    }
    
    /**
     * Validates choice index before processing to prevent race conditions.
     * @return true if valid, false otherwise
     */
    private fun validateChoiceIndex(node: Node.Choice, choiceIndex: Int): Boolean {
        return choiceIndex in 0 until node.options.size
    }

    fun initialize(graph: ChapterGraph, fastMode: Boolean = false) {
        currentGraph = graph
        isFastMode = fastMode
        statePersistence.setString(KEY_FAST_MODE, fastMode.toString())
        
        val savedNodeId = statePersistence.getString(KEY_CURRENT_NODE_ID)
        val currentNodeId = savedNodeId ?: graph.startNodeId
        
        statePersistence.setString(KEY_CURRENT_NODE_ID, currentNodeId)
        statePersistence.setString(KEY_CHAPTER_CODE, graph.chapterCode)
        
        _state.value = GameEngineState.Ready(
            chapterCode = graph.chapterCode,
            currentNodeId = currentNodeId
        )
    }

    suspend fun start() {
        val graph = currentGraph ?: return
        val currentNodeId = statePersistence.getString(KEY_CURRENT_NODE_ID) ?: graph.startNodeId
        
        _state.value = GameEngineState.Playing(
            chapterCode = graph.chapterCode,
            currentNodeId = currentNodeId,
            messages = emptyList()
        )

        executeNode(currentNodeId)
    }

    suspend fun resume() {
        isPaused = false
        statePersistence.getString(KEY_CURRENT_NODE_ID)?.let { executeNode(it) }
    }

    fun pause() {
        isPaused = true
    }

    suspend fun selectChoice(choiceIndex: Int) {
        val graph = currentGraph ?: return
        val currentId = statePersistence.getString(KEY_CURRENT_NODE_ID) ?: return

        val node = graph.getNode(currentId) as? Node.Choice ?: return
        
        if (!validateChoiceIndex(node, choiceIndex)) {
            return
        }
        
        val selectedOption = node.options[choiceIndex]

        statePersistence.setString(KEY_CURRENT_NODE_ID, selectedOption.targetNodeId)
        executeNode(selectedOption.targetNodeId)
    }

    /**
     * Main entry point for executing a node.
     * Dispatches to specific handlers based on node type.
     */
    private suspend fun executeNode(nodeId: String) {
        if (isPaused) return

        val graph = currentGraph ?: return
        statePersistence.setString(KEY_CURRENT_NODE_ID, nodeId)

        val node = graph.getNode(nodeId)
        if (node == null) {
            _state.value = GameEngineState.Error("Node not found: $nodeId")
            return
        }

        updateState(nodeId)

        when (node) {
            is Node.Message -> executeMessageNode(graph, node)
            is Node.Choice -> executeChoiceNode(node)
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

        // 3. SHOW MESSAGE
        val processedText = processMessageText(node.text)
        emit(
            GameEvent.ShowMessage(
                text = processedText,
                characterId = node.characterId,
                isMainCharacter = node.characterId == 0
            )
        )

        // 4. SAVE MEMORY AND NAVIGATE
        saveMemory()
        navigateToNext(graph, node, null)
    }

    /**
     * Executes a choice node - waits for user input.
     */
    private suspend fun executeChoiceNode(node: Node.Choice) {
        val options = node.options.map { it.text }
        emit(GameEvent.ShowChoices(options))
        emit(GameEvent.WaitingForInput)
        
        _state.value = GameEngineState.WaitingInput(
            chapterCode = currentGraph?.chapterCode ?: "",
            currentNodeId = node.id,
            messages = (_state.value as? GameEngineState.Playing)?.messages ?: emptyList()
        )
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

        saveMemory()
        navigateToNext(graph, node, handlerResult)
    }

    /**
     * Processes message text for variable substitution.
     */
    private fun processMessageText(text: String): String {
        return text.replace(Regex("\\[prenom\\]")) {
            memory.get("heroName") ?: "Hero"
        }
    }

    private suspend fun navigateToNext(
        graph: ChapterGraph,
        currentNode: Node,
        handlerResult: String?
    ) {
        val currentId = statePersistence.getString(KEY_CURRENT_NODE_ID) ?: return

        when (val result = navigator.resolveNextNode(graph, currentId, currentNode, handlerResult)) {
            is NodeNavigator.NavigationResult.NextNode -> {
                statePersistence.setString(KEY_CURRENT_NODE_ID, result.nodeId)
                executeNode(result.nodeId)
            }
            is NodeNavigator.NavigationResult.WaitingForInput -> {
                _state.value = GameEngineState.WaitingInput(
                    chapterCode = graph.chapterCode,
                    currentNodeId = currentId,
                    messages = (_state.value as? GameEngineState.Playing)?.messages ?: emptyList()
                )
            }
            is NodeNavigator.NavigationResult.ChapterComplete -> {
                _state.value = GameEngineState.Completed(graph.chapterCode)
            }
            is NodeNavigator.NavigationResult.Error -> {
                _state.value = GameEngineState.Error(result.message)
            }
        }
    }

    private fun updateState(nodeId: String) {
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
            is Node.Choice -> NodeType.CHOICE
            is Node.Condition -> NodeType.CONDITION
            is Node.Memory -> NodeType.MEMORY
            is Node.Info -> NodeType.INFO
            is Node.Trophy -> NodeType.TROPHY
            is Node.Signal -> NodeType.SIGNAL
            is Node.Background -> NodeType.BACKGROUND
        }
        return handlerFactory.getHandler(type)
    }

    private fun saveMemory() {
        memory.state.value.forEach { (key, value) ->
            statePersistence.setString("$KEY_MEMORY_PREFIX$key", value)
        }
    }

    private fun restoreMemory() {
        val memoryKeys = statePersistence.getKeys().filter { it.startsWith(KEY_MEMORY_PREFIX) }
        memoryKeys.forEach { key ->
            val memoryKey = key.removePrefix(KEY_MEMORY_PREFIX)
            val value = statePersistence.getString(key)
            if (memoryKey.isNotEmpty() && value != null) {
                memory.set(memoryKey, value)
            }
        }
    }

    private fun emit(event: GameEvent) {
        _events.trySend(event)
    }
}
