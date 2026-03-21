package com.purpletear.game.presentation.smsgame

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.MessageItem
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
    private val loadChapterGraphUseCase: LoadChapterGraphUseCase,
    private val gameEngine: GameEngine,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId: String = checkNotNull(savedStateHandle["gameId"]) {
        "gameId is required"
    }
    private val chapterCode: String = checkNotNull(savedStateHandle["chapterCode"]) {
        "chapterCode is required"
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Initialize engine and start collection in a supervised scope
        viewModelScope.launch {
            // Reset engine first to ensure clean state
            gameEngine.reset()

            // Launch collectors concurrently under supervision
            launch {
                gameEngine.state.collect { updateUiStateFromEngine(it) }
            }
            launch {
                gameEngine.messages.collect { updateMessages(it) }
            }
            launch {
                // Use collect (not collectLatest) to ensure no effects are dropped
                gameEngine.effects.collect { handleEffect(it) }
            }
            loadChapterGraph(gameId, chapterCode)
        }
    }

    private suspend fun loadChapterGraph(gameId: String, chapterCode: String) {
        Log.d("TEST", "Loading the chapter graph")
        loadChapterGraphUseCase(gameId, chapterCode, "fr-FR")
            .collectLatest { result ->
                result.fold(
                    onSuccess = { graph ->
                        Log.d("TEST", "Starting game")
                        startGame(gameId, graph)
                    },
                    onFailure = { error ->
                        Log.e("TEST", "Failed to load chapter graph : ${error.message}")
                        // TODO: implement error
                    }
                )
            }
    }

    private fun startGame(gameId: String, graph: ChapterGraph) {
        updateState {
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                isAwaitingInput = false
            )
        }

        viewModelScope.launch {
            gameEngine.initialize(gameId, graph)
            gameEngine.start()
        }
    }

    private fun updateUiStateFromEngine(engineState: GameEngineState) {
        when (engineState) {
            is GameEngineState.Idle -> {
                // TODO(implement)
            }

            is GameEngineState.Ready -> {
                // TODO(implement)
            }

            is GameEngineState.Playing -> {
                // TODO(implement)
            }

            is GameEngineState.AwaitingInput -> {
                // TODO(implement)
            }

            is GameEngineState.ChapterFinished -> {
                // TODO(implement)
            }

            is GameEngineState.Error -> {
                // TODO(implement)
            }
        }
    }

    private fun updateMessages(messages: List<GameMessage>) {
        val lastMessage = messages.lastOrNull()
        updateState {
            it.copy(
                messages = messages.map { msg ->
                    MessageItem(
                        id = msg.id,
                        text = msg.text,
                        characterId = msg.characterId,
                        isMainCharacter = msg.isMainCharacter
                    )
                },
            )
        }
    }

    /**
     * Handles one-shot effects emitted by the game engine.
     * This is where UI-specific actions are triggered.
     */
    private fun handleEffect(effect: HandlerEffect) {
        when (effect) {
            is HandlerEffect.ChangeBackground -> {

            }

            is HandlerEffect.PlaySound -> {
                // TODO: Trigger sound playback
                Log.d("GameEngine", "Play sound: ${effect.soundUrl}, loop: ${effect.loop}")
            }

            is HandlerEffect.StopSound -> {
                // TODO: Stop sound playback
                Log.d("GameEngine", "Stop sound")
            }

            is HandlerEffect.ShowGlitch -> {
                // TODO: Trigger glitch animation
                Log.d("GameEngine", "Show glitch for ${effect.durationMs}ms")
            }

            is HandlerEffect.SendSignal -> {
                // TODO: Handle signal (analytics, telemetry)
                Log.d("GameEngine", "Send signal: ${effect.action}")
            }

            is HandlerEffect.ScheduleNotification -> {
                // TODO: Schedule local notification
                Log.d("GameEngine", "Schedule notification: ${effect.title}")
            }

            is HandlerEffect.LoadImage -> {
                // TODO: Load and display image message
                Log.d("GameEngine", "Load image: ${effect.imageUrl}")
            }

            is HandlerEffect.SaveScore -> {
                // TODO: Save score remotely
                Log.d("GameEngine", "Save score: ${effect.score}")
            }

            is HandlerEffect.UnlockTrophy -> {
                // TODO: Unlock trophy
                Log.d("GameEngine", "Unlock trophy: ${effect.trophyId}")
            }

            is HandlerEffect.ShowChoices -> {
                updateState { it.copy(choices = effect.choices) }
            }

            is HandlerEffect.PlayVocal -> {
                // TODO: Play vocal audio
                Log.d("GameEngine", "Play vocal: ${effect.audioUrl}")
            }

            is HandlerEffect.ChangeChapter -> {
                // TODO: Handle chapter change
                Log.d("GameEngine", "Change chapter: ${effect.chapterCode}")
            }

            // Message effects are handled via messages StateFlow, not here
            is HandlerEffect.AddMessage,
            is HandlerEffect.UpdateLastMessageStatus,
            is HandlerEffect.UpdateMemory -> {
                // These are stateful effects handled by GameEngine internally
                // or via other StateFlows - no UI action needed
            }
        }
    }

    /**
     * Called when player selects a choice.
     */
    fun onChoiceSelected(choiceId: String) {
        // Clear choices immediately for UI responsiveness
        updateState { it.copy(choices = emptyList()) }
        viewModelScope.launch {
            gameEngine.onPlayerChoice(choiceId)
        }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }
}

data class GameUiState(
    val gameId: String? = null,
    val chapterCode: String? = null,
    val messages: List<MessageItem> = emptyList(),
    val choices: List<HandlerEffect.ShowChoices.Choice> = emptyList(),
    val isAwaitingInput: Boolean = false
)
