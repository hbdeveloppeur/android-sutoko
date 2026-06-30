package com.purpletear.game.presentation.game_play

import android.content.Context
import android.media.MediaPlayer
import android.os.Trace
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.debug.debugStartNodeFor
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.CharacterRepository
import com.purpletear.sutoko.game.repository.SceneRepository
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import com.purpletear.sutoko.game.usecase.GetSceneUseCase
import com.purpletear.sutoko.game.usecase.LoadChapterGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
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
    private val makeToastService: MakeToastService,
    private val storyTestingCoordinator: StoryTestingCoordinator,
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
    private val isTestMode: Boolean = savedStateHandle.get<Boolean>("isTestMode") ?: false

    private val _navigateToNextChapter = Channel<String>(Channel.BUFFERED)
    val navigateToNextChapter: Flow<String> = _navigateToNextChapter.receiveAsFlow()

    private var pendingChapterCode: String? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        Trace.beginSection("GameEngineViewModel.init")
        viewModelScope.launch {
            try {
                gameEngine.reset()

                val preloadScenes = launch {
                    sceneRepository.preload(gameId)
                }
                val preloadCharacters = launch {
                    characterRepository.preload(gameId)
                }

                launch { gameEngine.state.collect { updateUiStateFromEngine(it) } }
                launch { gameEngine.messages.collect { updateMessages(it) } }
                launch { gameEngine.effects.collect { handleEffect(it) } }

                if (isTestMode) {
                    observeStoryTestingState()
                } else {
                    loadChapterGraphAndStartGame(gameId, chapterCode)
                }

                preloadScenes.join()
                preloadCharacters.join()

                val characters = characterRepository.getAll().associateBy { it.id }
                updateState {
                    it.copy(characters = characters)
                }
            } finally {
                Trace.endSection()
            }
        }
    }

    private suspend fun loadChapterGraphAndStartGame(gameId: String, chapterCode: String) {
        // TODO: language
        loadChapterGraphUseCase(gameId, chapterCode, "fr-FR")
            .collectLatest { result ->
                result.fold(
                    onSuccess = { graph ->
                        startGame(gameId, graph)
                    },
                    onFailure = { error ->
                        Log.e("GameEngine", "Failed to load chapter $chapterCode: ${error.message}")
                        makeToastService(R.string.error_load_game)
                    }
                )
            }
    }

    private fun resetForNewPlay() {
        typingPlayer?.release()
        typingPlayer = null

        soundPlayer?.stop()
        soundPlayer?.release()
        soundPlayer = null

        vocalPlayer?.setOnCompletionListener(null)
        vocalPlayer?.release()
        vocalPlayer = null
        vocalProgressJob?.cancel()
        vocalProgressJob = null

        pendingChapterCode = null

        updateState {
            it.copy(
                messages = emptyList(),
                choices = emptyList(),
                isChoicesRevealed = false,
                isAwaitingInput = false,
                currentScene = null,
                currentVocalUrl = null,
                isVocalPlaying = false,
                vocalProgress = 0f
            )
        }
    }

    private fun startGame(gameId: String, graph: ChapterGraph, startNodeId: String? = null) {
        resetForNewPlay()

        updateState {
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                choices = emptyList(),
                isChoicesRevealed = false,
                isAwaitingInput = false,
                isLoadingStoryUpdates = true
            )
        }


        viewModelScope.launch {
            delay(1000)

            updateState {
                it.copy(
                    isLoadingStoryUpdates = false
                )
            }
            delay(280)
            gameEngine.initialize(gameId, graph)

            val debugStartNode = debugStartNodeFor(graph.chapterCode)
            when {
                startNodeId != null -> gameEngine.startFromNode(startNodeId)
                debugStartNode != null -> {
                    Log.d(
                        "GameEngine",
                        "Debug override — chapter ${graph.chapterCode} starts at $debugStartNode"
                    )
                    gameEngine.jumpToNode(debugStartNode)
                }

                else -> gameEngine.start()
            }
        }
    }

    private fun observeStoryTestingState() {
        StoryTestingLogger.i("NAV") { "GameEngineViewModel entering test mode — gameId=$gameId" }

        viewModelScope.launch {
            storyTestingCoordinator.state
                .map { it.isLoading }
                .distinctUntilChanged()
                .collectLatest { isLoading ->
                    updateState { it.copy(isLoadingStoryUpdates = isLoading) }
                }
        }

        viewModelScope.launch {
            var lastLoggedError: String? = null
            var lastPlayRequestCount = 0
            storyTestingCoordinator.state.collect { testingState ->
                testingState.error?.let { error ->
                    if (error != lastLoggedError) {
                        lastLoggedError = error
                        StoryTestingLogger.e("NAV") { "Story testing error: $error" }
                        makeToastService(R.string.error_load_game)
                    }
                } ?: run {
                    lastLoggedError = null
                }

                val graph = testingState.currentGraph
                val targetNodeId = testingState.targetNodeId
                if (graph != null && targetNodeId != null && testingState.playRequestCount != lastPlayRequestCount) {
                    StoryTestingLogger.i("NAV") { "Test mode starting game — ${graph.chapterCode} → $targetNodeId" }
                    lastPlayRequestCount = testingState.playRequestCount
                    startGame(gameId, graph, targetNodeId)
                }
            }
        }
    }

    private fun updateUiStateFromEngine(engineState: GameEngineState) {
        when (engineState) {
            is GameEngineState.AwaitingInput -> {
                updateState { it.copy(isAwaitingInput = true) }
            }

            is GameEngineState.Idle,
            is GameEngineState.Ready,
            is GameEngineState.Playing,
            is GameEngineState.ChapterFinished,
            is GameEngineState.Error -> {
                updateState {
                    it.copy(
                        isAwaitingInput = false,
                        choices = emptyList(),
                        isChoicesRevealed = false
                    )
                }
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

            is HandlerEffect.ShowChoices -> {
                updateState {
                    it.copy(
                        choices = effect.choices,
                        isChoicesRevealed = false
                    )
                }
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
        Trace.beginSection("GameEngineViewModel.onCleared")
        typingPlayer?.release()
        typingPlayer = null
        soundPlayer?.release()
        soundPlayer = null
        vocalPlayer?.release()
        vocalPlayer = null
        vocalProgressJob?.cancel()
        Trace.endSection()
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
        if (audioUrl.isBlank()) {
            Log.e("GameEngine", "Cannot play vocal: audioUrl is blank")
            return
        }

        if (!File(audioUrl).exists()) {
            Log.e("GameEngine", "Cannot play vocal: file not found at $audioUrl")
        }

        vocalPlayer?.setOnCompletionListener(null)
        vocalPlayer?.release()
        vocalProgressJob?.cancel()

        vocalPlayer = try {
            MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
                setOnCompletionListener {
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
            updateState {
                it.copy(
                    currentVocalUrl = audioUrl,
                    isVocalPlaying = true,
                    vocalProgress = 0f
                )
            }
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

    fun onChoiceSelected(choice: HandlerEffect.ShowChoices.Choice) {
        val nextNodeId = choice.nextNodeId ?: return
        viewModelScope.launch {
            gameEngine.submitChoice(nextNodeId)
        }
    }

    fun onRevealChoicesClicked() {
        updateState { it.copy(isChoicesRevealed = true) }
    }

    fun onHideChoicesClicked() {
        updateState { it.copy(isChoicesRevealed = false) }
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }
}


