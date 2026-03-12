package com.purpletear.game.presentation.smsgame.engine

import androidx.lifecycle.SavedStateHandle
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
 * State survives process death via SavedStateHandle.
 */
class GameEngine @Inject constructor(
    private val handlerFactory: NodeHandlerFactory,
    private val navigator: NodeNavigator,
    private val savedStateHandle: SavedStateHandle
) {
    companion object {
        private const val KEY_CURRENT_NODE_ID = "current_node_id"
        private const val KEY_CHAPTER_CODE = "chapter_code"
        private const val KEY_MEMORY_PREFIX = "memory_"
    }

    private val _state = MutableStateFlow<GameEngineState>(GameEngineState.Idle)
    val state: StateFlow<GameEngineState> = _state.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events: Flow<GameEvent> = _events.receiveAsFlow()

    val memory = GameMemory()
    private var currentGraph: ChapterGraph? = null
    private var isPaused = false

    init {
        // Restore memory from SavedStateHandle
        restoreMemory()
    }
    
    /**
     * Validates choice index before processing to prevent race conditions.
     * @return true if valid, false otherwise
     */
    private fun validateChoiceIndex(node: Node.Choice, choiceIndex: Int): Boolean {
        return choiceIndex in 0 until node.options.size
    }

    fun initialize(graph: ChapterGraph) {
        currentGraph = graph
        
        // Restore or set current node
        val savedNodeId = savedStateHandle.get<String>(KEY_CURRENT_NODE_ID)
        val currentNodeId = savedNodeId ?: graph.startNodeId
        
        savedStateHandle[KEY_CURRENT_NODE_ID] = currentNodeId
        savedStateHandle[KEY_CHAPTER_CODE] = graph.chapterCode
        
        _state.value = GameEngineState.Ready(
            chapterCode = graph.chapterCode,
            currentNodeId = currentNodeId
        )
    }

    suspend fun start() {
        val graph = currentGraph ?: return
        val currentNodeId = savedStateHandle.get<String>(KEY_CURRENT_NODE_ID) ?: graph.startNodeId
        
        _state.value = GameEngineState.Playing(
            chapterCode = graph.chapterCode,
            currentNodeId = currentNodeId,
            messages = emptyList()
        )

        executeNode(currentNodeId)
    }

    suspend fun resume() {
        isPaused = false
        savedStateHandle.get<String>(KEY_CURRENT_NODE_ID)?.let { executeNode(it) }
    }

    fun pause() {
        isPaused = true
    }

    suspend fun selectChoice(choiceIndex: Int) {
        val graph = currentGraph ?: return
        val currentId = savedStateHandle.get<String>(KEY_CURRENT_NODE_ID) ?: return

        val node = graph.getNode(currentId) as? Node.Choice ?: return
        
        // Validate index before processing - prevents race conditions
        if (!validateChoiceIndex(node, choiceIndex)) {
            return
        }
        
        val selectedOption = node.options[choiceIndex]

        savedStateHandle[KEY_CURRENT_NODE_ID] = selectedOption.targetNodeId
        executeNode(selectedOption.targetNodeId)
    }

    private suspend fun executeNode(nodeId: String) {
        if (isPaused) return

        val graph = currentGraph ?: return
        savedStateHandle[KEY_CURRENT_NODE_ID] = nodeId

        val node = graph.getNode(nodeId)
        if (node == null) {
            _state.value = GameEngineState.Error("Node not found: $nodeId")
            return
        }

        updateState(nodeId)

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

        // Save memory after each node execution
        saveMemory()

        navigateToNext(graph, node, handlerResult)
    }

    private suspend fun navigateToNext(
        graph: ChapterGraph,
        currentNode: Node,
        handlerResult: String?
    ) {
        val currentId = savedStateHandle.get<String>(KEY_CURRENT_NODE_ID) ?: return

        when (val result = navigator.resolveNextNode(graph, currentId, currentNode, handlerResult)) {
            is NodeNavigator.NavigationResult.NextNode -> {
                savedStateHandle[KEY_CURRENT_NODE_ID] = result.nodeId
                executeNode(result.nodeId)
            }
            is NodeNavigator.NavigationResult.WaitingForInput -> {
                _state.value = GameEngineState.WaitingInput(
                    chapterCode = graph.chapterCode,
                    currentNodeId = currentId,
                    messages = (state.value as? GameEngineState.Playing)?.messages ?: emptyList()
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
            savedStateHandle["$KEY_MEMORY_PREFIX$key"] = value
        }
    }

    private fun restoreMemory() {
        // Only iterate keys that start with our prefix - bounded complexity
        val memoryKeys = savedStateHandle.keys().filter { it.startsWith(KEY_MEMORY_PREFIX) }
        memoryKeys.forEach { key ->
            val memoryKey = key.removePrefix(KEY_MEMORY_PREFIX)
            val value = savedStateHandle.get<String>(key)
            // Defensive: validate key isn't empty after prefix removal
            if (memoryKey.isNotEmpty() && value != null) {
                memory.set(memoryKey, value)
            }
        }
    }
}
