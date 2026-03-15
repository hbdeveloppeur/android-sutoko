package com.purpletear.game.presentation.smsgame

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameEvent
import com.purpletear.sutoko.game.engine.MessageItem
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.core.presentation.extensions.awaitFlowResult
import com.purpletear.sutoko.game.usecase.GetGameUseCase
import com.purpletear.sutoko.game.usecase.LoadChapterGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SmsGamePlayViewModel @Inject constructor(
    private val loadChapterGraph: LoadChapterGraphUseCase,
    private val getGame: GetGameUseCase,
    private val gameEngine: GameEngine,
    private val downloadManager: GameDownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
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
        updateState { 
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                backgroundImage = null,
                isLoading = false,
                error = null,
                isCompleted = false,
                isTyping = false
            )
        }

        viewModelScope.launch {
            gameEngine.initialize(gameId, graph, isFastMode)
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
                // TODO: Implement signal handling
            }
            is GameEvent.ChangeChapter -> {
                updateState { it.copy(nextChapterCode = event.chapterCode) }
            }
            is GameEvent.ShowTypingIndicator -> {
                updateState { it.copy(isTyping = true, typingCharacterId = event.characterId) }
            }
        }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }

    /**
     * Clears all downloaded game data, re-downloads from server, and reinitializes.
     * Use this for dev/testing to force a fresh download.
     */
    fun clearGameDataAndReinitialize(gameId: String, chapterCode: String) {
        viewModelScope.launch {
            Log.d("TEST", "Clearing game data for: $gameId")
            
            // Step 1: Clear local data
            downloadManager.clearGameData(gameId)
            
            // Step 2: Reset UI state to loading
            _uiState.value = GameUiState(isLoading = true)
            
            // Step 3: Get game metadata from server
            Log.d("TEST", "Fetching game metadata for: $gameId")
            val game = try {
                awaitFlowResult { getGame(gameId) }
            } catch (e: Exception) {
                Log.e("TEST", "Failed to get game metadata", e)
                _uiState.value = GameUiState(error = "Failed to get game: ${e.message}")
                return@launch
            }
            
            // Step 4: Download the game
            Log.d("TEST", "Starting download for game: ${game.id}")
            downloadManager.downloadGame(game)
            
            // Step 5: Wait for terminal state (Completed, Error, or Cancelled)
            val finalState = downloadManager.getDownloadState(gameId).first { state ->
                state is GameDownloadState.Completed || 
                state is GameDownloadState.Error || 
                state is GameDownloadState.Cancelled
            }
            
            when (finalState) {
                GameDownloadState.Completed -> {
                    Log.d("TEST", "Download completed, reinitializing...")
                    initialize(gameId, chapterCode)
                }
                is GameDownloadState.Error -> {
                    Log.e("TEST", "Download failed: ${finalState.cause.message}")
                    _uiState.value = GameUiState(error = "Download failed: ${finalState.cause.message}")
                }
                GameDownloadState.Cancelled -> {
                    Log.d("TEST", "Download cancelled")
                    _uiState.value = GameUiState(error = "Download cancelled")
                }
                else -> { /* unreachable */ }
            }
        }
    }
}

data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val nextChapterCode: String? = null,
    val messages: List<MessageItem> = emptyList(),
    val backgroundImage: String? = null,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val typingCharacterId: Int? = null
)
