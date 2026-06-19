package com.purpletear.game.presentation.game_play

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.CharacterRepository
import com.purpletear.sutoko.game.repository.SceneRepository
import com.purpletear.sutoko.game.usecase.GetSceneUseCase
import com.purpletear.sutoko.game.usecase.LoadChapterGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
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
    private val characterRepository: CharacterRepository,
    private val getSceneUseCase: GetSceneUseCase,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var typingPlayer: MediaPlayer? = null
    private var soundPlayer: MediaPlayer? = null
    private var vocalPlayer: MediaPlayer? = null
    private var vocalProgressJob: Job? = null

    private val gameId: String = checkNotNull(savedStateHandle["gameId"]) {
        "gameId is required"
    }
    private val chapterCode: String = checkNotNull(savedStateHandle["chapterCode"]) {
        "chapterCode is required"
    }

    private val _navigateToNextChapter = Channel<String>(Channel.BUFFERED)
    val navigateToNextChapter: Flow<String> = _navigateToNextChapter.receiveAsFlow()

    private var pendingChapterCode: String? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gameEngine.reset()

            // Preload scenes and characters in parallel with chapter graph loading
            val preloadScenes = launch {
                sceneRepository.preload(gameId)
            }
            val preloadCharacters = launch {
                characterRepository.preload(gameId)
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

            preloadScenes.join()
            preloadCharacters.join()

            val characters = characterRepository.getAll().associateBy { it.id }
            updateState {
                it.copy(characters = characters)
            }
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
                        Log.e("GameEngine", "Failed to load chapter $chapterCode: ${error.message}")
                        updateState { it.copy(errorMessage = "Failed to load chapter: ${error.message}") }
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

            is HandlerEffect.PlayTypingSound -> playTypingSound()

            is HandlerEffect.PlaySound -> playSound(effect.soundUrl, effect.loop)

            is HandlerEffect.PlayVocal -> playVocal(effect.audioUrl)

            is HandlerEffect.StopSound -> stopSound()

            is HandlerEffect.ChangeChapter -> {
                pendingChapterCode = effect.chapterCode
            }

            else -> {
                // TODO: Implement effect handling when needed
                Log.d("GameEngine", "Received effect: ${effect::class.simpleName}")
            }
        }
    }

    private fun playTypingSound() {
        typingPlayer?.release()
        typingPlayer = MediaPlayer.create(context, R.raw.typing)?.apply {
            setOnCompletionListener {
                release()
                typingPlayer = null
            }
            start()
        }
    }

    override fun onCleared() {
        typingPlayer?.release()
        typingPlayer = null
        soundPlayer?.release()
        soundPlayer = null
        vocalPlayer?.release()
        vocalPlayer = null
        vocalProgressJob?.cancel()
        super.onCleared()
    }

    private fun playSound(soundUrl: String, loop: Boolean) {
        soundPlayer?.release()
        soundPlayer = try {
            MediaPlayer().apply {
                setDataSource(soundUrl)
                isLooping = loop
                prepare()
                setOnCompletionListener {
                    if (!loop) {
                        release()
                        soundPlayer = null
                    }
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play sound: $soundUrl", e)
            null
        }
    }

    fun onVocalClicked(audioUrl: String) {
        val state = _uiState.value
        if (state.currentVocalUrl == audioUrl && state.isVocalPlaying) {
            pauseVocal()
        } else {
            playVocal(audioUrl)
        }
    }

    private fun pauseVocal() {
        vocalPlayer?.pause()
        vocalProgressJob?.cancel()
        updateState { it.copy(isVocalPlaying = false) }
    }

    private fun playVocal(audioUrl: String) {
        vocalPlayer?.setOnCompletionListener(null)
        vocalPlayer?.release()
        vocalProgressJob?.cancel()

        vocalPlayer = try {
            MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
                setOnCompletionListener {
                    // Identity check: only the currently active player may mutate shared state.
                    // Prevents a stale listener from a previously released player from nuking
                    // the reference to a newly started player.
                    if (vocalPlayer === this) {
                        release()
                        vocalPlayer = null
                        vocalProgressJob?.cancel()
                        updateState { state ->
                            state.copy(isVocalPlaying = false, vocalProgress = 1f)
                        }
                    }
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play vocal: $audioUrl", e)
            null
        }

        if (vocalPlayer != null) {
            updateState { it.copy(currentVocalUrl = audioUrl, isVocalPlaying = true, vocalProgress = 0f) }
            startVocalProgressTracking()
        }
    }

    private fun startVocalProgressTracking() {
        vocalProgressJob?.cancel()
        vocalProgressJob = viewModelScope.launch {
            while (isActive) {
                val player = vocalPlayer
                val duration = player?.duration?.takeIf { it > 0 }
                val position = player?.currentPosition?.takeIf { it >= 0 }
                if (duration != null && position != null) {
                    val progress = position.toFloat() / duration.toFloat()
                    updateState { it.copy(vocalProgress = progress.coerceIn(0f, 1f)) }
                }
                delay(100)
            }
        }
    }

    private fun stopSound() {
        soundPlayer?.stop()
        soundPlayer?.release()
        soundPlayer = null
    }

    private fun handleChangeScene(effect: HandlerEffect.ChangeScene) {
        viewModelScope.launch {
            val scene = getSceneUseCase(effect.sceneId)
            this@GameEngineViewModel.updateState { it.copy(currentScene = scene) }
        }
    }

    fun onNextChapterClicked() {
        pendingChapterCode?.let { _navigateToNextChapter.trySend(it) }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }
}


