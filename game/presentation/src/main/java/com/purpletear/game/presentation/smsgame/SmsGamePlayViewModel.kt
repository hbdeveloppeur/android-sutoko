package com.purpletear.game.presentation.smsgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.game.presentation.smsgame.engine.GameEngine
import com.purpletear.game.presentation.smsgame.engine.GameEngineState
import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.MessageItem
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.usecase.LoadChapterGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SmsGamePlayViewModel @Inject constructor(
    private val loadChapterGraph: LoadChapterGraphUseCase,
    private val gameEngine: GameEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // State and events are collected separately.
        // Events cause UI updates (messages, choices); State reflects engine mode.
        // Ordering: Events are emitted during node execution, state updates after.
        // Both flows are independent - UI must handle events arriving before/after state changes.
        viewModelScope.launch {
            gameEngine.state.collectLatest { engineState ->
                updateUiStateFromEngine(engineState)
            }
        }

        viewModelScope.launch {
            gameEngine.events.collectLatest { event ->
                handleGameEvent(event)
            }
        }
    }

    fun initialize(gameId: String, chapterCode: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            loadChapterGraph(gameId, chapterCode)
        }
    }

    fun onChoiceSelected(choiceIndex: Int) {
        viewModelScope.launch {
            updateState { it.copy(choices = null) }
            gameEngine.selectChoice(choiceIndex)
        }
    }

    private suspend fun loadChapterGraph(gameId: String, chapterCode: String) {
        // TODO : change language
        loadChapterGraph(gameId, chapterCode, "fr-FR")
            .collectLatest { result ->
                result.fold(
                    onSuccess = { graph ->
                        startGame(gameId, graph)
                    },
                    onFailure = { error ->
                        updateState { 
                            it.copy(
                                isLoading = false, 
                                error = error.message ?: "Failed to load chapter"
                            )
                        }
                    }
                )
            }
    }

    private fun startGame(gameId: String, graph: ChapterGraph) {
        gameEngine.initialize(graph)
        
        updateState { 
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                choices = null,
                backgroundImage = null,
                isLoading = false,
                error = null,
                isCompleted = false
            )
        }

        viewModelScope.launch {
            gameEngine.start()
        }
    }

    private fun updateUiStateFromEngine(engineState: GameEngineState) {
        when (engineState) {
            is GameEngineState.Idle -> updateState { it.copy(isLoading = true) }
            is GameEngineState.Ready -> updateState { it.copy(isLoading = false) }
            is GameEngineState.Playing -> updateState { 
                it.copy(
                    isLoading = false,
                    chapterCode = engineState.chapterCode,
                    isWaitingForInput = false
                )
            }
            is GameEngineState.WaitingInput -> updateState { 
                it.copy(
                    isWaitingForInput = true,
                    chapterCode = engineState.chapterCode
                )
            }
            is GameEngineState.Completed -> updateState { 
                it.copy(
                    isCompleted = true,
                    chapterCode = engineState.chapterCode
                )
            }
            is GameEngineState.Error -> updateState { 
                it.copy(
                    isLoading = false,
                    error = engineState.message
                )
            }
        }
    }

    private suspend fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.ShowMessage -> {
                val newMessage = MessageItem(
                    id = UUID.randomUUID().toString(),
                    text = event.text,
                    characterId = event.characterId,
                    isMainCharacter = event.isMainCharacter
                )
                updateState { it.copy(messages = it.messages + newMessage) }
            }
            is GameEvent.ShowChoices -> {
                updateState { it.copy(choices = event.options) }
            }
            is GameEvent.ShowInfo -> {
                val newMessage = MessageItem(
                    id = UUID.randomUUID().toString(),
                    text = event.text,
                    characterId = -1,
                    isMainCharacter = false
                )
                updateState { it.copy(messages = it.messages + newMessage) }
            }
            is GameEvent.ChangeBackground -> {
                updateState { it.copy(backgroundImage = event.imageUrl) }
            }
            is GameEvent.UnlockTrophy -> {
                // TODO: Implement trophy unlock - persist to repository, show notification
            }
            is GameEvent.SendSignal -> {
                // TODO: Implement signal handling - analytics, side effects, etc.
            }
            is GameEvent.ChangeChapter -> {
                updateState { it.copy(nextChapterCode = event.chapterCode) }
            }
            is GameEvent.WaitingForInput -> {
                // Event received - UI updates via state change, no action needed here
            }
        }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun calculateNextChapterCode(currentCode: String): String {
        return "2b"
    }
}

data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val nextChapterCode: String? = null,
    val messages: List<MessageItem> = emptyList(),
    val choices: List<String>? = null,
    val backgroundImage: String? = null,
    val isLoading: Boolean = true,
    val isWaitingForInput: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)
