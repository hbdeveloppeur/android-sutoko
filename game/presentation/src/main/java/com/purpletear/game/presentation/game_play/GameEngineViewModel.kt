package com.purpletear.game.presentation.game_play

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.SceneRepository
import com.purpletear.sutoko.game.usecase.GetSceneUseCase
import com.purpletear.sutoko.game.usecase.LoadChapterGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for game engine interaction.
 * Manages the game engine state, messages, and effects during gameplay.
 */
@HiltViewModel
class GameEngineViewModel @Inject constructor(
    private val loadChapterGraphUseCase: LoadChapterGraphUseCase,
    private val gameEngine: GameEngine,
    private val sceneRepository: SceneRepository,
    private val getSceneUseCase: GetSceneUseCase,
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
        viewModelScope.launch {
            gameEngine.reset()

            // Preload scenes in parallel with chapter graph loading
            val preloadJob = launch {
                sceneRepository.preload(gameId)
            }

            launch {
                gameEngine.state.collect { updateUiStateFromEngine(it) }
            }
            launch {
                gameEngine.messages.collect { updateMessages(it) }
            }
            launch {
                gameEngine.effects.collect { handleEffect(it) }
            }

            loadChapterGraph(gameId, chapterCode)

            preloadJob.join()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            sceneRepository.clear()
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
            is GameEngineState.Idle,
            is GameEngineState.Ready,
            is GameEngineState.Playing,
            is GameEngineState.AwaitingInput,
            is GameEngineState.ChapterFinished,
            is GameEngineState.Error -> {
                // TODO: Handle specific engine states when needed
            }
        }
    }

    private fun updateMessages(messages: List<GameMessage>) {
        updateState {
            it.copy(
                messages = messages,
            )
        }
    }

    /**
     * Handles one-shot effects emitted by the game engine.
     */
    private fun handleEffect(effect: HandlerEffect) {
        when (effect) {

            is HandlerEffect.ChangeScene -> handleChangeScene(effect)

            else -> {
                // TODO: Implement effect handling when needed
                Log.d("GameEngine", "Received effect: ${effect::class.simpleName}")
            }
        }
    }

    private fun handleChangeScene(effect: HandlerEffect.ChangeScene) {
        viewModelScope.launch {
            val scene = getSceneUseCase(effect.sceneId)
            this@GameEngineViewModel.updateState { it.copy(currentScene = scene) }
        }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }
}


