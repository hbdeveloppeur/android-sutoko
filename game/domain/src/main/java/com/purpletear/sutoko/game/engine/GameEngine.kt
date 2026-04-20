package com.purpletear.sutoko.game.engine

import android.util.Log
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.message.GameMessageNextChapter
import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

class GameEngine @Inject constructor(
    private val handlerFactory: NodeHandlerFactory,
    private val nodeResolver: NodeResolver,
    private val memory: GameMemory,
    private val timingScheduler: TimingScheduler
) {

    private val _state = MutableStateFlow<GameEngineState>(GameEngineState.Idle)
    val state: StateFlow<GameEngineState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<GameMessage>>(emptyList())
    val messages: StateFlow<List<GameMessage>> = _messages.asStateFlow()

    private val _effects = MutableSharedFlow<HandlerEffect>(
        replay = 10,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val effects: Flow<HandlerEffect> = _effects.asSharedFlow()

    private var currentGraph: ChapterGraph? = null
    private var currentGameId: String? = null
    private val inputMutex = Mutex()
    private var isPaused = false
    private var awaitingInput = false

    /**
     * Resets the engine to a clean state.
     * MUST be called before initialize() to ensure no state leakage from previous sessions.
     */
    fun reset() {
        isPaused = false
        awaitingInput = false
        currentGraph = null
        currentGameId = null
        _state.value = GameEngineState.Idle
        _messages.value = emptyList()
    }

    /**
     * Initializes the engine for a game session.
     * Loads persisted memory from database.
     *
     * Precondition: reset() should be called before initialize() for a new session
     *
     * @param gameId Unique identifier for the game
     * @param graph The chapter graph to execute
     * @throws IllegalStateException if already initialized with a different game
     */
    suspend fun initialize(gameId: String, graph: ChapterGraph) {

        // TODO : weirdo - it must reset when chapter changes, right?
        if (currentGameId != null && currentGameId != gameId) {
            reset()
        }

        currentGameId = gameId
        currentGraph = graph

        // Load persisted memory from database
        memory.load(gameId)

        _state.value = GameEngineState.Ready(
            chapterCode = graph.chapterCode,
            currentNodeId = graph.startNodeId
        )
    }

    /**
     * Starts the game execution from the beginning of the chapter.
     *
     * Precondition: initialize() must be called before start()
     *
     * @throws IllegalStateException if engine not initialized
     */
    suspend fun start() {
        val graph = checkNotNull(currentGraph) {
            "Precondition violation: initialize() must be called before start()"
        }
        val currentNodeId = graph.startNodeId

        _state.value = GameEngineState.Playing(
            chapterCode = graph.chapterCode,
            currentNodeId = currentNodeId,
        )
        _messages.value = emptyList()
        awaitingInput = false
        executeNode(currentNodeId)
    }

    /**
     * Main entry point for executing a node.
     * Orchestrates: preparation -> script building -> execution -> navigation.
     *
     * @param nodeId The node ID to execute
     */
    private suspend fun executeNode(nodeId: String) {
        assert(nodeId.isNotBlank())

        if (isPaused) return

        val context = prepareExecutionContext(nodeId) ?: return
        updateCurrentNodeId(nodeId)

        val script = buildScript(context) ?: return

        val shouldContinue = executeScript(script, context)
        if (!shouldContinue) return

        navigateToNext(context.node, script.nextNodeId)
    }

    /**
     * Prepares the execution context by resolving graph, node, and handler.
     * Returns null if preparation fails (error state already set).
     */
    private fun prepareExecutionContext(nodeId: String): ExecutionContext? {
        val graph = currentGraph ?: run {
            _state.value = GameEngineState.Error("Engine not initialized - call initialize() first")
            return null
        }

        val node = graph.getNode(nodeId) ?: run {
            _state.value = GameEngineState.Error("Node not found: $nodeId")
            return null
        }

        val handler = getHandler(node)
        return ExecutionContext(graph, node, nodeId, handler)
    }

    /**
     * Builds the handler script with exception handling.
     * Returns null if script building fails (error state already set).
     */
    private fun buildScript(context: ExecutionContext): HandlerScript? {
        return try {
            context.handler.buildScript(context.node, memory)
        } catch (e: IllegalStateException) {
            _state.value = GameEngineState.Error("State error: ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            _state.value = GameEngineState.Error("Invalid argument: ${e.message}")
            null
        } catch (e: IndexOutOfBoundsException) {
            _state.value = GameEngineState.Error("Index out of bounds: ${e.message}")
            null
        }
    }

    /**
     * Executes the script commands sequentially.
     *
     * @return true if script completed normally, false if paused/awaiting input
     */
    private suspend fun executeScript(
        script: HandlerScript,
        context: ExecutionContext
    ): Boolean {
        for (command in script.commands) {
            if (isPaused) return false

            val shouldContinue = executeCommand(command, script, context)
            if (!shouldContinue) return false
        }
        return true
    }

    /**
     * Executes a single command.
     *
     * @return true to continue script execution, false to halt (awaiting input)
     */
    private suspend fun executeCommand(
        command: HandlerCommand,
        script: HandlerScript,
        context: ExecutionContext
    ): Boolean {
        return when (command) {
            is HandlerCommand.Emit -> {
                applyEffect(command.effect)
                true
            }

            is HandlerCommand.Delay -> {
                timingScheduler.delay(command.millis)
                true
            }

            is HandlerCommand.AwaitInput -> {
                awaitPlayerInput(command, script, context)
                false
            }
        }
    }

    /**
     * Handles the AwaitInput command: validates position, sets state, and pauses.
     */
    private fun awaitPlayerInput(
        command: HandlerCommand.AwaitInput,
        script: HandlerScript,
        context: ExecutionContext
    ) {
        val commandIndex = script.commands.indexOf(command)

        check(commandIndex == script.commands.lastIndex) {
            "Invariant violation: AwaitInput must be the last command. " +
                    "Found ${script.commands.size - commandIndex - 1} orphaned commands."
        }

        _state.value = GameEngineState.AwaitingInput(
            chapterCode = context.graph.chapterCode,
            currentNodeId = context.nodeId
        )

        awaitingInput = true
    }

    private suspend fun applyEffect(effect: HandlerEffect) {
        when (effect) {
            is HandlerEffect.AddMessage -> {
                _messages.value += effect.message
            }

            is HandlerEffect.DeleteMessage -> {
                _messages.value = _messages.value.filter { it.id != effect.messageId }
            }


            is HandlerEffect.UpdateMemory -> {
                memory.set(effect.key, effect.value)
            }

            is HandlerEffect.ChangeChapter -> {
                memory.setCurrentChapter(effect.chapterCode)
                memory.save()
                _messages.value += GameMessageNextChapter()
            }

            is HandlerEffect.StoryFinished -> {
                _messages.value += GameMessageInfo("end_story", "Story finished!")
            }

            else -> {
                val emitted = _effects.tryEmit(effect)
                if (!emitted) {
                    Log.w("GameEngine", "Effect dropped (buffer full): $effect")
                }
            }
        }
    }

    private suspend fun navigateToNext(
        currentNode: Node,
        explicitNextNodeId: String?
    ) {
        val graph = checkNotNull(currentGraph) {
            "Precondition violation: currentGraph is null during navigation"
        }

        when (val result = nodeResolver.resolveNextNode(graph, currentNode, explicitNextNodeId)) {
            is NodeResolver.ResolutionResult.NextNode -> {
                executeNode(result.nodeId)
            }

            is NodeResolver.ResolutionResult.NodeNextChapter -> {
                _state.value = GameEngineState.ChapterFinished(graph.chapterCode)
            }

            is NodeResolver.ResolutionResult.Error -> {
                _state.value = GameEngineState.Error(result.message)
            }
        }
    }

    private fun updateCurrentNodeId(nodeId: String) {
        val currentState = state.value
        check(currentState is GameEngineState.Playing) {
            "Precondition violation: expected Playing state to update node ID, got $currentState"
        }
        _state.value = currentState.copy(currentNodeId = nodeId)
    }

    private fun getHandler(node: Node): NodeHandler = when (node) {
        is Node.Start -> handlerFactory.getHandler(NodeType.START)
        is Node.Message -> handlerFactory.getHandler(NodeType.MESSAGE)
        is Node.MessageImage -> handlerFactory.getHandler(NodeType.MESSAGE_IMAGE)
        is Node.ChapterChange -> handlerFactory.getHandler(NodeType.CHAPTER_CHANGE)
        is Node.Condition -> handlerFactory.getHandler(NodeType.CONDITION)
        is Node.Memory -> handlerFactory.getHandler(NodeType.MEMORY)
        is Node.Info -> handlerFactory.getHandler(NodeType.INFO)
        is Node.Trophy -> handlerFactory.getHandler(NodeType.TROPHY)
        is Node.Signal -> handlerFactory.getHandler(NodeType.SIGNAL)
        is Node.Background -> handlerFactory.getHandler(NodeType.BACKGROUND)
        is Node.ConversationModeChange -> handlerFactory.getHandler(NodeType.CONVERSATION_MODE_CHANGE)
        is Node.Scene -> handlerFactory.getHandler(NodeType.SCENE)
        is Node.End -> handlerFactory.getHandler(NodeType.END)
    }
}
