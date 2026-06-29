package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.BuildConfig
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.message.GameMessageNextChapter
import com.purpletear.sutoko.game.engine.processing.TextProcessor
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
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class GameEngine @Inject constructor(
    private val handlerFactory: NodeHandlerFactory,
    private val nodeResolver: NodeResolver,
    private val memory: GameMemory,
    private val timingScheduler: TimingScheduler,
    private val textProcessor: TextProcessor,
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
    private var availableChoices: List<HandlerEffect.ShowChoices.Choice> = emptyList()

    /**
     * Resets the engine to a clean state.
     * MUST be called before initialize() to ensure no state leakage from previous sessions.
     */
    fun reset() {
        isPaused = false
        awaitingInput = false
        availableChoices = emptyList()
        currentGraph = null
        currentGameId = null
        _state.value = GameEngineState.Idle
        _messages.value = emptyList()
        GameEngineLogger.d("GAME") { "Engine reset" }
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

        if (currentGameId != null && currentGameId != gameId) {
            reset()
        }

        currentGameId = gameId
        currentGraph = graph

        // Tag new memories written during this session with the current chapter,
        // then load only state from earlier or the same chapter. Memories saved
        // in this chapter or any later chapter are deleted first to prevent
        // overlap when replaying.
        memory.setCurrentChapterNumber(graph.chapterNumber)
        memory.setCurrentChapter(graph.chapterCode)
        memory.load(gameId, graph.chapterNumber)

        _state.value = GameEngineState.Ready(
            chapterCode = graph.chapterCode,
            currentNodeId = graph.startNodeId
        )

        GameEngineLogger.d("GAME") {
            "Initialized — gameId=$gameId, chapter=${graph.chapterCode}, number=${graph.chapterNumber}, startNode=${graph.startNodeId}"
        }
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
        availableChoices = emptyList()

        GameEngineLogger.d("GAME") { "Starting chapter ${graph.chapterCode} at node $currentNodeId" }
        executeNode(currentNodeId)
    }

    /**
     * Starts execution from an arbitrary node in the current chapter.
     *
     * Clears transient input/message state and executes the target node as if the engine had
     * just navigated to it. This is the production-safe entry point used by real-time testing
     * when the author asks the phone to play from a specific node.
     *
     * Precondition: [initialize] must have been called.
     * Precondition: [nodeId] must exist in the current graph.
     *
     * @throws IllegalStateException if preconditions are violated.
     */
    suspend fun startFromNode(nodeId: String) {
        assert(nodeId.isNotBlank())

        val graph = checkNotNull(currentGraph) {
            "Precondition violation: initialize() must be called before startFromNode()"
        }
        checkNotNull(graph.getNode(nodeId)) {
            "Jump target not found in chapter ${graph.chapterCode}: $nodeId"
        }

        awaitingInput = false
        availableChoices = emptyList()
        _messages.value = emptyList()

        _state.value = GameEngineState.Playing(
            chapterCode = graph.chapterCode,
            currentNodeId = nodeId,
        )

        GameEngineLogger.d("GAME") { "Start from node — chapter ${graph.chapterCode} → node $nodeId" }
        executeNode(nodeId)
    }

    /**
     * Debug-only: jumps to an arbitrary node in the current chapter.
     *
     * Delegates to [startFromNode] after verifying the build is debug.
     *
     * @throws IllegalStateException in release builds or when preconditions are violated.
     */
    suspend fun jumpToNode(nodeId: String) {
        check(BuildConfig.DEBUG) { "jumpToNode is a debug-only API" }
        startFromNode(nodeId)
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

        GameEngineLogger.d("NODE") {
            "Executing ${context.nodeId} (${context.node::class.simpleName}) in chapter ${context.graph.chapterCode}"
        }

        val script = buildScript(context) ?: return

        GameEngineLogger.d("HAND") {
            "${context.handler::class.simpleName} → ${script.commands.size} commands, nextNodeId=${script.nextNodeId}"
        }

        val shouldContinue = executeScript(script, context)
        if (!shouldContinue) return

        navigateToNext(context, script.nextNodeId)
    }

    /**
     * Prepares the execution context by resolving graph, node, and handler.
     * Returns null if preparation fails (error state already set).
     */
    private fun prepareExecutionContext(nodeId: String): ExecutionContext? {
        assert(nodeId.isNotBlank())

        val graph = currentGraph ?: run {
            GameEngineLogger.e("ERR") { "Engine not initialized — call initialize() first" }
            _state.value = GameEngineState.Error("Engine not initialized - call initialize() first")
            return null
        }

        val node = graph.getNode(nodeId) ?: run {
            GameEngineLogger.e("ERR") { "Node not found: $nodeId" }
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
            GameEngineLogger.e(
                "ERR",
                e
            ) { "State error building script for node ${context.nodeId}" }
            _state.value = GameEngineState.Error("State error: ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            GameEngineLogger.e(
                "ERR",
                e
            ) { "Invalid argument building script for node ${context.nodeId}" }
            _state.value = GameEngineState.Error("Invalid argument: ${e.message}")
            null
        } catch (e: IndexOutOfBoundsException) {
            GameEngineLogger.e(
                "ERR",
                e
            ) { "Index out of bounds building script for node ${context.nodeId}" }
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
                GameEngineLogger.d("CMD") { "Emit ${command.effect::class.simpleName}" }
                applyEffect(command.effect)
                true
            }

            is HandlerCommand.Delay -> {
                GameEngineLogger.d("CMD") { "Delay ${command.millis}ms" }
                timingScheduler.delay(command.millis)
                true
            }

            is HandlerCommand.AwaitInput -> {
                GameEngineLogger.d("CMD") { "AwaitInput with ${command.choices.size} choices" }
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

        enterAwaitingInput(context, command.choices)
    }

    private fun enterAwaitingInput(
        context: ExecutionContext,
        choices: List<HandlerEffect.ShowChoices.Choice>
    ) {
        _state.value = GameEngineState.AwaitingInput(
            chapterCode = context.graph.chapterCode,
            currentNodeId = context.nodeId
        )

        awaitingInput = true
        availableChoices = choices

        GameEngineLogger.d("INPT") {
            "Pausing for choice at ${context.nodeId} — ${choices.size} options: ${choices.map { it.nextNodeId }}"
        }
    }

    /**
     * Resumes execution after the player selected a choice.
     *
     * @param nextNodeId The target node id of the selected choice
     * @throws IllegalStateException if the engine is not awaiting input
     * @throws IllegalArgumentException if the id does not match any offered choice
     */
    suspend fun submitChoice(nextNodeId: String) {
        inputMutex.withLock {
            check(awaitingInput && state.value is GameEngineState.AwaitingInput) {
                "Precondition violation: submitChoice() called while engine is not awaiting input"
            }

            val validIds = availableChoices.map { it.nextNodeId }
            check(nextNodeId in validIds) {
                GameEngineLogger.e("INPT") { "Invalid choice: $nextNodeId is not one of $validIds" }
                "Invalid choice: $nextNodeId is not one of the offered choices"
            }

            GameEngineLogger.d("INPT") { "Choice selected: $nextNodeId" }

            awaitingInput = false
            availableChoices = emptyList()
            executeNode(nextNodeId)
        }
    }

    private suspend fun applyEffect(effect: HandlerEffect) {
        when (effect) {
            is HandlerEffect.AddMessage -> {
                GameEngineLogger.d("FX") { "Add ${effect.message::class.simpleName}" }
                _messages.value += effect.message
            }

            is HandlerEffect.DeleteMessage -> {
                GameEngineLogger.d("FX") { "Delete message ${effect.messageId}" }
                _messages.value = _messages.value.filter { it.id != effect.messageId }
            }

            is HandlerEffect.UpdateMemory -> {
                GameEngineLogger.d("FX") { "Update memory: ${effect.key}=${effect.value}" }
                memory.set(effect.key, effect.value)
            }

            is HandlerEffect.ChangeChapter -> {
                GameEngineLogger.d("FX") { "Change chapter: ${effect.chapterCode}" }
                memory.setCurrentChapter(effect.chapterCode)
                memory.save()
                _messages.value += GameMessageNextChapter()
                val emitted = _effects.tryEmit(effect)
                if (!emitted) {
                    GameEngineLogger.w("FX") { "Effect dropped (buffer full): $effect" }
                }
            }

            is HandlerEffect.StoryFinished -> {
                GameEngineLogger.i("FX") { "Story finished" }
                _messages.value += GameMessageInfo("end_story", "Story finished!")
            }

            else -> {
                val emitted = _effects.tryEmit(effect)
                if (!emitted) {
                    GameEngineLogger.w("FX") { "Effect dropped (buffer full): $effect" }
                }
            }
        }
    }

    private suspend fun navigateToNext(
        context: ExecutionContext,
        explicitNextNodeId: String?
    ) {
        explicitNextNodeId?.let {
            assert(it.isNotBlank())
        }

        val graph = checkNotNull(currentGraph) {
            "Precondition violation: currentGraph is null during navigation"
        }

        when (val result = nodeResolver.resolveNextNode(graph, context.node, explicitNextNodeId)) {
            is NodeResolver.ResolutionResult.NextNode -> {
                val reason = explicitNextNodeId?.let { "explicit nextNodeId" }
                    ?: "edge from ${context.node.id}"
                GameEngineLogger.d("NAV") { "→ ${result.nodeId} ($reason)" }
                executeNode(result.nodeId)
            }

            is NodeResolver.ResolutionResult.NodeNextChapter -> {
                GameEngineLogger.d("NAV") { "Chapter ${graph.chapterCode} finished at ${context.node.id}" }
                _state.value = GameEngineState.ChapterFinished(graph.chapterCode)
            }

            is NodeResolver.ResolutionResult.AwaitChoice -> {
                val variables = memory.state.value
                val processedChoices = result.choices.map { choice ->
                    choice.copy(text = textProcessor.process(choice.text, variables))
                }
                GameEngineLogger.d("NAV") { "Showing ${processedChoices.size} choices from ${context.node.id}" }
                applyEffect(HandlerEffect.ShowChoices(processedChoices))
                enterAwaitingInput(context, processedChoices)
            }

            is NodeResolver.ResolutionResult.Error -> {
                GameEngineLogger.e("NAV") { "Navigation error from ${context.node.id}: ${result.message}" }
                _state.value = GameEngineState.Error(result.message)
            }
        }
    }

    private fun updateCurrentNodeId(nodeId: String) {
        val currentState = state.value
        val chapterCode = when (currentState) {
            is GameEngineState.Playing -> currentState.chapterCode
            is GameEngineState.AwaitingInput -> currentState.chapterCode
            else -> error("Precondition violation: expected Playing or AwaitingInput state to update node ID, got $currentState")
        }
        _state.value = GameEngineState.Playing(
            chapterCode = chapterCode,
            currentNodeId = nodeId
        )
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
        is Node.Background -> handlerFactory.getHandler(NodeType.BACKGROUND)
        is Node.ConversationModeChange -> handlerFactory.getHandler(NodeType.CONVERSATION_MODE_CHANGE)
        is Node.Scene -> handlerFactory.getHandler(NodeType.SCENE)
        is Node.End -> handlerFactory.getHandler(NodeType.END)
        is Node.Sound -> handlerFactory.getHandler(NodeType.SOUND)
        is Node.MessageVocal -> handlerFactory.getHandler(NodeType.MESSAGE_VOCAL)
    }
}
