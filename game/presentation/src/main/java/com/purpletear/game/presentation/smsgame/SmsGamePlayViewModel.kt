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

    fun onNextChapter() {
        val currentState = _uiState.value
        val currentCode = currentState.chapterCode ?: return
        
        val nextCode = calculateNextChapterCode(currentCode)
        
        viewModelScope.launch {
            loadChapterGraph(currentState.gameId ?: return@launch, nextCode)
        }
    }

    private suspend fun loadChapterGraph(gameId: String, chapterCode: String) {
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
                    id = System.currentTimeMillis().toString(),
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
                    id = System.currentTimeMillis().toString(),
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
                // Handle trophy unlock
            }
            is GameEvent.SendSignal -> {
                // Handle signal
            }
            is GameEvent.ChangeChapter -> {
                // Handle chapter change
            }
            is GameEvent.WaitingForInput -> {
                // Input is now waiting
            }
        }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun calculateNextChapterCode(currentCode: String): String {
        val number = currentCode.filter { it.isDigit() }.toIntOrNull() ?: 1
        val alternative = currentCode.filter { it.isLetter() }.ifEmpty { "a" }
        return "${number + 1}${alternative}"
    }
}

data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val messages: List<MessageItem> = emptyList(),
    val choices: List<String>? = null,
    val backgroundImage: String? = null,
    val isLoading: Boolean = true,
    val isWaitingForInput: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)
