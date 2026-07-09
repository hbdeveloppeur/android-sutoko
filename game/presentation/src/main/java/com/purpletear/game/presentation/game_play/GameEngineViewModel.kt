package com.purpletear.game.presentation.game_play

import android.content.Context
import android.media.MediaPlayer
import android.os.Trace
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.debug.SmsGameDebugNodeJumps
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.liveupdate.StoryLiveUpdateConnectionState
import com.purpletear.game.presentation.game_play.liveupdate.StoryLiveUpdateCoordinator
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.game.presentation.game_play.state.LiveUpdateStatus
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val storyLiveUpdateCoordinator: StoryLiveUpdateCoordinator,
    private val logger: Logger,
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
    private val isLiveUpdateMode: Boolean =
        savedStateHandle.get<Boolean>(SmsGameRoutes.IS_LIVE_UPDATE_MODE_ARG) ?: false

    private val _navigateToNextChapter = Channel<String>(Channel.BUFFERED)
    val navigateToNextChapter: Flow<String> = _navigateToNextChapter.receiveAsFlow()

    private var pendingChapterCode: String? = null

    private var lastPlayRequestCount = 0
    private var lastGraphVersion = 0
    private var hasStartedGame = false

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        Trace.beginSection("GameEngineViewModel.init")
        updateState {
            it.copy(
                isLiveUpdateMode = isLiveUpdateMode,
                showNextChapterButton = !isLiveUpdateMode,
                nextChapterTitleRes = if (isLiveUpdateMode) R.string.message_next_chapter_test_mode_title else null
            )
        }
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

                if (isLiveUpdateMode) {
                    observeStoryLiveUpdateState()
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
                        val debugJumpNodeId = if (BuildConfig.DEBUG) {
                            SmsGameDebugNodeJumps.getNodeId(graph.chapterCode)
                        } else {
                            null
                        }

                        if (debugJumpNodeId != null) {
                            startGameWithDebugJump(gameId, graph, debugJumpNodeId)
                        } else {
                            startGame(gameId, graph)
                        }
                    },
                    onFailure = { error ->
                        logger.exception(error) { "Failed to load chapter $chapterCode" }
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

    /**
     * Initializes the engine and starts playback. If [startNodeId] is provided but does not
     * exist in [graph] (e.g. a stale PLAY_FROM_NODE request), it is ignored and playback falls
     * back to the chapter start node instead of crashing.
     */
    private fun startGame(
        gameId: String,
        graph: ChapterGraph,
        startNodeId: String? = null,
        showLoadingOverlay: Boolean = true,
    ) {
        resetForNewPlay()

        updateState {
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                choices = emptyList(),
                isChoicesRevealed = false,
                isAwaitingInput = false,
                isLoadingStoryUpdates = showLoadingOverlay
            )
        }

        viewModelScope.launch {
            try {
                if (showLoadingOverlay) {
                    delay(1000)
                    updateState { it.copy(isLoadingStoryUpdates = false) }
                    delay(280)
                }

                gameEngine.initialize(gameId, graph)

                // Tolerate an untrusted/unknown node (e.g. PLAY_FROM_NODE): never crash.
                val safeStartNodeId = resolveStartNodeId(graph, startNodeId)
                if (startNodeId != null && safeStartNodeId == null) {
                    logger.exception(IllegalArgumentException(startNodeId)) {
                        "PLAY_FROM_NODE target missing in ${graph.chapterCode}: $startNodeId - fallback to start"
                    }
                    makeToastService(R.string.error_play_from_node_missing)
                }

                when {
                    safeStartNodeId != null -> gameEngine.startFromNode(safeStartNodeId)
                    else -> gameEngine.start()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.exception(e) { "startGame failed for ${graph.chapterCode}" }
                if (BuildConfig.DEBUG) throw e
                makeToastService(R.string.error_load_game)
            }
        }
    }

    private fun startGameWithDebugJump(
        gameId: String,
        graph: ChapterGraph,
        nodeId: String,
    ) {
        resetForNewPlay()

        updateState {
            it.copy(
                gameId = gameId,
                chapterCode = graph.chapterCode,
                messages = emptyList(),
                choices = emptyList(),
                isChoicesRevealed = false,
                isAwaitingInput = false,
                isLoadingStoryUpdates = false
            )
        }

        viewModelScope.launch {
            try {
                checkNotNull(graph.getNode(nodeId)) {
                    "Debug jump target not found in chapter ${graph.chapterCode}: $nodeId"
                }
                gameEngine.initialize(gameId, graph)
                gameEngine.jumpToNode(nodeId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.exception(e) { "startGameWithDebugJump failed for ${graph.chapterCode}" }
                if (BuildConfig.DEBUG) throw e
                makeToastService(R.string.error_load_game)
            }
        }
    }

    private fun observeStoryLiveUpdateState() {
        StoryTestingLogger.i("NAV") { "GameEngineViewModel entering test mode — gameId=$gameId" }

        viewModelScope.launch {
            var lastLoggedError: String? = null
            storyLiveUpdateCoordinator.state.collect { testingState ->
                val status = when {
                    !testingState.isActive -> null
                    testingState.isLoading -> LiveUpdateStatus.Loading
                    testingState.connectionState == StoryLiveUpdateConnectionState.CONNECTED -> LiveUpdateStatus.Connected
                    testingState.connectionState == StoryLiveUpdateConnectionState.DISCONNECTED -> LiveUpdateStatus.Disconnected
                    else -> null
                }
                updateState { it.copy(liveUpdateStatus = status) }

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

                // Explicit author request: always restart from the requested node.
                if (graph != null && targetNodeId != null && testingState.playRequestCount != lastPlayRequestCount) {
                    StoryTestingLogger.i("NAV") { "Test mode explicit play — ${graph.chapterCode} → $targetNodeId" }
                    lastPlayRequestCount = testingState.playRequestCount
                    lastGraphVersion = testingState.graphVersion
                    updateState { it.copy(hasPendingStoryUpdate = false) }
                    hasStartedGame = true
                    startGame(gameId, graph, targetNodeId, showLoadingOverlay = false)
                    return@collect
                }

                if (graph != null && testingState.graphVersion != lastGraphVersion) {
                    lastGraphVersion = testingState.graphVersion
                    if (!hasStartedGame) {
                        val initialChapterId = testingState.initialChapterId
                        if (initialChapterId != null && graph.chapterCode != initialChapterId) {
                            StoryTestingLogger.d("NAV") { "Test mode initial start skipped — ${graph.chapterCode} is not the initial chapter $initialChapterId" }
                            return@collect
                        }
                        StoryTestingLogger.i("NAV") { "Test mode initial start — ${graph.chapterCode} → ${graph.startNodeId}" }
                        hasStartedGame = true
                        startGame(gameId, graph, graph.startNodeId, showLoadingOverlay = false)
                    } else if (graph.chapterCode == _uiState.value.chapterCode) {
                        StoryTestingLogger.i("NAV") { "Test mode graph updated — reload available" }
                        updateState { it.copy(hasPendingStoryUpdate = true) }
                    } else {
                        StoryTestingLogger.d("NAV") { "Test mode graph update ignored — ${graph.chapterCode} is not current chapter" }
                    }
                }
            }
        }
    }

    private fun updateUiStateFromEngine(engineState: GameEngineState) {
        when (engineState) {
            is GameEngineState.AwaitingInput -> {
                updateState { it.copy(isAwaitingInput = true) }
            }

            is GameEngineState.Playing -> {
                updateState {
                    it.copy(
                        isAwaitingInput = false,
                        choices = emptyList(),
                        isChoicesRevealed = false
                    )
                }
            }

            is GameEngineState.Ready -> {
                updateState {
                    it.copy(
                        isAwaitingInput = false,
                        choices = emptyList(),
                        isChoicesRevealed = false
                    )
                }
            }

            is GameEngineState.Idle,
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

    /**
     * Applies a graph update that arrived while the engine was already running.
     * Playback resets and resumes from the last known node if it still exists in the new graph,
     * otherwise from the chapter start node.
     */
    fun onReloadStoryUpdates() {
        if (!_uiState.value.hasPendingStoryUpdate) return

        val graph = storyLiveUpdateCoordinator.state.value.currentGraph ?: return
        val resumeNodeId = graph.startNodeId

        StoryTestingLogger.i("NAV") { "Test mode reloading — ${graph.chapterCode} → $resumeNodeId" }
        updateState { it.copy(hasPendingStoryUpdate = false) }
        startGame(gameId, graph, resumeNodeId, showLoadingOverlay = false)
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }
}

/**
 * Returns [requestedNodeId] when it exists in [graph], or `null` when it is null/unknown so the
 * caller can fall back to the chapter start node. Pure policy that keeps the engine's strict
 * precondition from being reached with untrusted node ids (e.g. PLAY_FROM_NODE).
 */
internal fun resolveStartNodeId(graph: ChapterGraph, requestedNodeId: String?): String? =
    requestedNodeId?.takeIf { graph.getNode(it) != null }


