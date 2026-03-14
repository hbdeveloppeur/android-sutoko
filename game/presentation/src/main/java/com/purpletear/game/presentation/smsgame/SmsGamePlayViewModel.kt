package com.purpletear.game.presentation.smsgame

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameEvent
import com.purpletear.sutoko.game.engine.MessageItem
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
        Log.d("TEST", "Calling initialisation")
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
        Log.d("TEST", "Loading the chapter graph")
        // TODO : change language
        loadChapterGraph(gameId, chapterCode, "fr-FR")
            .collectLatest { result ->
                result.fold(
                    onSuccess = { graph ->
                        Log.d("TEST", "Starting game")
                        startGame(gameId, graph)
                    },
                    onFailure = { error ->
                        Log.e("TEST", "Failed to load chapter graph : ${error.message}")
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

    private fun startGame(gameId: String, graph: ChapterGraph, isFastMode: Boolean = false) {
        gameEngine.initialize(graph, isFastMode)
        
        updateState { 
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                choices = null,
                backgroundImage = null,
                isLoading = false,
                error = null,
                isCompleted = false,
                isTyping = false
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

    private fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.ShowMessage -> {
                val newMessage = MessageItem(
                    id = UUID.randomUUID().toString(),
                    text = event.text,
                    characterId = event.characterId,
                    isMainCharacter = event.isMainCharacter
                )
                updateState { it.copy(messages = it.messages + newMessage, isTyping = false) }
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

            is GameEvent.SendSignal -> {

            }
            is GameEvent.ChangeChapter -> {
                updateState { it.copy(nextChapterCode = event.chapterCode) }
            }
            is GameEvent.WaitingForInput -> {
                // Event received - UI updates via state change, no action needed here
            }
            is GameEvent.ShowTypingIndicator -> {
                updateState { it.copy(isTyping = true, typingCharacterId = event.characterId) }
            }
        }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
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
    val error: String? = null,
    val isTyping: Boolean = false,
    val typingCharacterId: Int? = null
)
