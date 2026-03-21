package com.purpletear.sutoko.game.engine

import android.util.Log
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

/**
 * Core game engine that manages node execution and state.
 * State survives process death via GameSessionStorage (provided by presentation layer).
 * Game memory is persisted to database at explicit save points.
 *
 * All node-specific processing is delegated to handlers via NodeHandlerFactory.
 * The engine focuses purely on orchestration: node resolution, navigation, and state management.
 *
 * Architecture:
 * - GameEngine: Orchestrates node execution, owns state
 * - NodeHandler: Decides what happens for a node type (pure logic)
 * - NodeResolver: Determines next node based on graph edges
 * - GameMemory: Holds game variables, persisted at save points
 *
 * Lifecycle:
 * - One engine instance per game session (ViewModel-scoped)
 * - Properly isolated between sessions to prevent state corruption
 * - Not a singleton to avoid memory leaks and state pollution
 */
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

    /**
     * One-shot effects emitted by the engine for the presentation layer.
     * For stateful data (messages), subscribe to GameEngine.messages instead.
     *
     * Uses BUFFERED to prevent effect loss under load. SharedFlow with replay
     * ensures effects survive ViewModel recreation during config changes.
     */
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
        // Defensive: ensure clean state even if reset() was forgotten
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
     * Called when player makes a choice.
     * Thread-safe and resumes execution if engine was paused for input.
     *
     * @param choiceId The choice identifier (typically "0", "1", etc. for edge index)
     */
    suspend fun onPlayerChoice(choiceId: String) {
        val capturedChoice = inputMutex.withLock {
            if (!awaitingInput) {
                return  // Silently ignore if not awaiting input
            }
            awaitingInput = false
            choiceId  // Capture inside lock to prevent race conditions
        }

        // Resume execution outside the lock to avoid deadlock
        resumeExecution(capturedChoice)
    }

    /**
     * Resumes execution after player input.
     *
     * @param choice The captured choice value (passed explicitly to avoid race conditions)
     */
    private suspend fun resumeExecution(choice: String) {
        val graph = checkNotNull(currentGraph) {
            "Precondition violation: engine not initialized"
        }

        val currentState = checkNotNull(state.value as? GameEngineState.AwaitingInput) {
            "Precondition violation: expected AwaitingInput state, got ${state.value}"
        }

        // Map choice to actual node via graph edges
        val currentNodeId = currentState.currentNodeId
        val targetNodeId = resolveChoiceToNode(graph, currentNodeId, choice)
            ?: run {
                _state.value =
                    GameEngineState.Error("Invalid choice '$choice' from node '$currentNodeId'")
                return
            }

        executeNode(targetNodeId)
    }

    /**
     * Resolves a player choice to the target node ID.
     *
     * @param graph The chapter graph
     * @param currentNodeId The current node ID
     * @param choiceId The choice identifier from player input
     * @return The target node ID, or null if choice is invalid
     */
    private fun resolveChoiceToNode(
        graph: ChapterGraph,
        currentNodeId: String,
        choiceId: String
    ): String? {
        // Try to interpret choiceId as a direct node ID first
        if (graph.getNode(choiceId) != null) {
            return choiceId
        }

        // Try to interpret as edge index ("0", "1", etc.)
        val choiceIndex = choiceId.toIntOrNull()
        if (choiceIndex != null) {
            return graph.getNextNode(currentNodeId, choiceIndex)
        }

        return null
    }

    /**
     * Main entry point for executing a node.
     * Dispatches to handlers based on node type.
     * Handlers return HandlerScript (commands) which the engine executes step by step.
     * This enables immediate effect emission and resume-capable execution.
     *
     * @param nodeId The node ID to execute
     * @throws IllegalStateException if engine not initialized
     */
    private suspend fun executeNode(nodeId: String) {
        if (isPaused) return

        val graph = checkNotNull(currentGraph) {
            "Precondition violation: engine not initialized - call initialize() first"
        }

        val node = graph.getNode(nodeId)
            ?: run {
                _state.value = GameEngineState.Error("Node not found: $nodeId")
                return
            }

        updateCurrentNodeId(nodeId)

        val handler = getHandler(node)

        val script = try {
            handler.prepare(node, memory)
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

        // Execute commands sequentially - each effect is applied immediately
        for (command in script.commands) {
            if (isPaused) return

            when (command) {
                is HandlerCommand.Emit -> applyEffect(command.effect)

                is HandlerCommand.Delay -> {
                    timingScheduler.delay(command.millis)
                }

                is HandlerCommand.AwaitInput -> {
                    // Defensive: AwaitInput MUST be last - commands after it would be orphaned
                    val commandIndex = script.commands.indexOf(command)
                    check(commandIndex == script.commands.lastIndex) {
                        "Invariant violation: AwaitInput must be the last command in a script. " +
                                "Found ${script.commands.size - commandIndex - 1} orphaned commands after AwaitInput."
                    }

                    // Set state atomically to prevent race conditions
                    _state.value = GameEngineState.AwaitingInput(
                        chapterCode = graph.chapterCode,
                        currentNodeId = nodeId
                    )
                    inputMutex.withLock {
                        awaitingInput = true
                    }
                    return
                }
            }
        }

        navigateToNext(node, script.nextNodeId)
    }

    private fun applyEffect(effect: HandlerEffect) {
        when (effect) {
            is HandlerEffect.AddMessage -> {
                _messages.value += effect.message
            }

            is HandlerEffect.UpdateLastMessageStatus -> {
                val current = _messages.value
                if (current.isNotEmpty()) {
                    _messages.value =
                        current.dropLast(1) + current.last().copy(status = effect.status)
                }
            }

            is HandlerEffect.UpdateMemory -> {
                memory.set(effect.key, effect.value)
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
        is Node.ChapterChange -> handlerFactory.getHandler(NodeType.CHAPTER_CHANGE)
        is Node.Condition -> handlerFactory.getHandler(NodeType.CONDITION)
        is Node.Memory -> handlerFactory.getHandler(NodeType.MEMORY)
        is Node.Info -> handlerFactory.getHandler(NodeType.INFO)
        is Node.Trophy -> handlerFactory.getHandler(NodeType.TROPHY)
        is Node.Signal -> handlerFactory.getHandler(NodeType.SIGNAL)
        is Node.Background -> handlerFactory.getHandler(NodeType.BACKGROUND)
    }
}
