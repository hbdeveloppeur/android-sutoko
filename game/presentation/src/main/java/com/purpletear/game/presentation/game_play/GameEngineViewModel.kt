package com.purpletear.game.presentation.game_play

import android.content.Context
import android.media.MediaPlayer
import android.os.Trace
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.data.infrastructure.SystemTimingScheduler
import com.purpletear.game.debug.SmsGameDebugNodeJumps
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.state.FakeNotificationUi
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.extractCinematicBody
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.CharacterRepository
import com.purpletear.sutoko.game.repository.SceneRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for game engine interaction.
 * Manages the game engine state, messages, and effects during gameplay.
 */
@HiltViewModel
class GameEngineViewModel @Inject constructor(
    private val loadChapterGraphUseCase: LoadChapterGraphUseCase,
    private val gameEngine: GameEngine,
    private val timingScheduler: SystemTimingScheduler,
    private val sceneRepository: SceneRepository,
    private val characterRepository: CharacterRepository,
    private val chapterRepository: ChapterRepository,
    private val gameRepository: GameRepository,
    private val getSceneUseCase: GetSceneUseCase,
    private val mediaUrlResolver: MediaUrlResolver,
    private val makeToastService: MakeToastService,
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
    private val isTrial: Boolean =
        savedStateHandle.get<Boolean>(SmsGameRoutes.IS_TRIAL_ARG) ?: false

    private val _navigateToNextChapter = Channel<String>(Channel.BUFFERED)
    val navigateToNextChapter: Flow<String> = _navigateToNextChapter.receiveAsFlow()

    private val _navigateToCinematic = Channel<Unit>(Channel.BUFFERED)
    val navigateToCinematic: Flow<Unit> = _navigateToCinematic.receiveAsFlow()

    private val _navigateToBuy = Channel<Unit>(Channel.BUFFERED)
    val navigateToBuy: Flow<Unit> = _navigateToBuy.receiveAsFlow()

    private val _navigateToExit = Channel<Unit>(Channel.BUFFERED)
    val navigateToExit: Flow<Unit> = _navigateToExit.receiveAsFlow()

    private var cinematicResumeNodeId: String? = null

    private var pendingChapterCode: String? = null
    private var currentGraph: ChapterGraph? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        Trace.beginSection("GameEngineViewModel.init")
        // The scheduler is a process-wide @Singleton: never inherit a stale hold from a
        // previous session.
        timingScheduler.setHoldPaused(false)
        updateState {
            it.copy(
                isTrial = isTrial,
                isChoicesDarkMode = readChoicesDarkMode()
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
                launch {
                    gameRepository.observeGame(gameId)
                        .catch { e -> logger.exception(e) { "observeGame logo failed" } }
                        .collect { catalog ->
                            updateState {
                                it.copy(
                                    gameLogoUrl = mediaUrlResolver.resolveBannerUrl(catalog?.logo?.storagePath)
                                )
                            }
                        }
                }

                loadChapterGraphAndStartGame(gameId, chapterCode)

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
        loadChapterGraphUseCase(gameId, chapterCode, Locale.getDefault().toLanguageTag())
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
                        makeToastService(R.string.game_presentation_error_load_game)
                    }
                )
            }
    }

    private fun resetForNewPlay() {
        timingScheduler.setHoldPaused(false)
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
                vocalProgress = 0f,
                isHoldPaused = false
            )
        }
    }

    /**
     * Initializes the engine and starts playback from the chapter start node.
     */
    private fun startGame(
        gameId: String,
        graph: ChapterGraph,
    ) {
        resetForNewPlay()
        currentGraph = graph

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
            try {
                delay(1000)
                updateState { it.copy(isLoadingStoryUpdates = false) }
                delay(280)

                gameEngine.initialize(gameId, graph)
                gameEngine.start()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.exception(e) { "startGame failed for ${graph.chapterCode}" }
                if (BuildConfig.DEBUG) throw e
                makeToastService(R.string.game_presentation_error_load_game)
            }
        }
    }

    private fun startGameWithDebugJump(
        gameId: String,
        graph: ChapterGraph,
        nodeId: String,
    ) {
        resetForNewPlay()
        currentGraph = graph

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
                makeToastService(R.string.game_presentation_error_load_game)
            }
        }
    }

    private fun updateUiStateFromEngine(engineState: GameEngineState) {
        when (engineState) {
            is GameEngineState.AwaitingInput -> {
                updateState { it.copy(isAwaitingInput = true) }
            }

            is GameEngineState.AwaitingMangaDismissal -> {
                updateState { it.copy(isMangaActive = true) }
            }

            is GameEngineState.Playing -> {
                updateState {
                    it.copy(
                        isAwaitingInput = false,
                        choices = emptyList(),
                        isChoicesRevealed = false,
                        isMangaActive = false
                    )
                }
            }

            is GameEngineState.Ready -> {
                updateState {
                    it.copy(
                        isAwaitingInput = false,
                        choices = emptyList(),
                        isChoicesRevealed = false,
                        isMangaActive = false
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
                        isChoicesRevealed = false,
                        isMangaActive = false
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
                checkNextChapterAvailability(effect.chapterCode)
            }

            is HandlerEffect.ShowChoices -> {
                updateState {
                    it.copy(
                        choices = effect.choices,
                        isChoicesRevealed = false
                    )
                }
            }

            is HandlerEffect.EnterCinematic -> enterCinematic(effect)

            is HandlerEffect.ShowFakeNotification -> handleShowFakeNotification(effect)

            else -> {
                Log.d("GameEngine", "Received effect: ${effect::class.simpleName}")
            }
        }
    }

    /**
     * Resolves the availability of the next chapter targeted by a chapter change.
     * Fail-closed: a missing chapter or a failed lookup is treated as unavailable
     * (navigating there would be a dead end anyway).
     */
    private fun checkNextChapterAvailability(nextChapterCode: String) {
        viewModelScope.launch {
            val next = chapterRepository.observeChapters(gameId)
                .catch { e ->
                    logger.exception(e) { "next chapter availability check failed" }
                    emit(emptyList())
                }
                .first()
                .firstOrNull { it.normalizedCode == nextChapterCode.lowercase() }
            updateState {
                it.copy(
                    isNextChapterAvailable = next?.available == true,
                    nextChapterReleaseDate = next?.releaseDate?.takeIf { date -> date > 0 },
                )
            }
        }
    }

    private fun playTypingSound() {
        typingPlayer?.release()
        typingPlayer = MediaPlayer.create(context, R.raw.game_presentation_typing)?.apply {
            setOnCompletionListener {
                release()
                typingPlayer = null
            }
            start()
        }
    }

    override fun onCleared() {
        Trace.beginSection("GameEngineViewModel.onCleared")
        timingScheduler.setHoldPaused(false)
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

    private fun handleShowFakeNotification(effect: HandlerEffect.ShowFakeNotification) {
        val avatarPath = effect.imageUrl
            ?: effect.characterId?.let { _uiState.value.characters[it]?.avatar }
        updateState {
            it.copy(
                fakeNotification = FakeNotificationUi(
                    title = effect.title,
                    subtitle = effect.subtitle,
                    actionText = effect.actionText,
                    avatarPath = avatarPath,
                    durationMs = effect.durationMs
                )
            )
        }
    }

    /**
     * Called when the fake notification overlay has finished its exit animation.
     * The notification is decorative: the engine resumes on its own timing, this only
     * clears the UI state.
     */
    fun onFakeNotificationDismissed() {
        updateState { it.copy(fakeNotification = null) }
    }

    private fun handleChangeScene(effect: HandlerEffect.ChangeScene) {
        viewModelScope.launch {
            val scene = getSceneUseCase(effect.sceneId)
            this@GameEngineViewModel.updateState { it.copy(currentScene = scene) }
        }
    }

    fun onNextChapterClicked() {
        if (!_uiState.value.isNextChapterAvailable) return
        pendingChapterCode?.let { _navigateToNextChapter.trySend(it) }
    }

    fun onBackClicked() {
        _navigateToExit.trySend(Unit)
    }

    fun onChoiceSelected(choice: HandlerEffect.ShowChoices.Choice) {
        val nextNodeId = choice.nextNodeId ?: return
        viewModelScope.launch {
            gameEngine.submitChoice(nextNodeId)
        }
    }

    /**
     * Called when the player dismisses the manga page (Close / back / tap-outside). Resumes the
     * engine so the next node is resolved. Safe to call for any page dismiss: the engine no-ops
     * when it is not parked on a manga page (e.g. re-opening a historical page).
     */
    fun onMangaPageDismissed() {
        viewModelScope.launch {
            gameEngine.resumeFromMangaPage()
        }
    }

    /**
     * Hold-to-pause: freezes the engine's pacing while the player keeps a finger on the
     * screen, and resumes on release. The freeze happens inside the timing scheduler, so
     * in-flight scripts are never dropped (unlike GameEngine.pause()). Ignored while the
     * engine is not actively streaming messages (choices, cinematic, manga page).
     */
    fun onHoldPauseChanged(held: Boolean) {
        val state = _uiState.value
        if (state.isHoldPaused == held) return
        if (held && (state.isAwaitingInput || state.isCinematicActive || state.isMangaActive)) return
        timingScheduler.setHoldPaused(held)
        updateState { it.copy(isHoldPaused = held) }
    }

    fun onRevealChoicesClicked() {
        updateState { it.copy(isChoicesRevealed = true) }
    }

    fun onHideChoicesClicked() {
        updateState { it.copy(isChoicesRevealed = false) }
    }

    fun onToggleChoicesDarkMode() {
        val next = !_uiState.value.isChoicesDarkMode
        updateState { it.copy(isChoicesDarkMode = next) }
        writeChoicesDarkMode(next)
    }

    private fun readChoicesDarkMode(): Boolean =
        context.getSharedPreferences(CHOICES_PREFS_FILE, Context.MODE_PRIVATE)
            .getBoolean(CHOICES_DARK_MODE_KEY, true)

    private fun writeChoicesDarkMode(isDarkMode: Boolean) {
        context.getSharedPreferences(CHOICES_PREFS_FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(CHOICES_DARK_MODE_KEY, isDarkMode)
            .apply()
    }

    /**
     * Loads a scene for the cinematic player. Delegates to the same use case the SMS engine uses,
     * so `scene-node` frames render identically via `SceneComposable`.
     */
    suspend fun loadScene(sceneId: Int) = getSceneUseCase(sceneId)

    /**
     * Called by `CinematicScreen` when the cinematic body is exhausted (or cancelled). Resumes the
     * SMS engine at the node after `[intro=end]` and clears the cinematic slice.
     */
    fun onCinematicFinished() = resumeFromCinematic()

    private fun resumeFromCinematic() {
        val resumeNodeId = cinematicResumeNodeId
        cinematicResumeNodeId = null
        updateState { it.copy(cinematicBody = emptyList(), isCinematicActive = false) }

        gameEngine.resume()

        if (resumeNodeId != null) {
            viewModelScope.launch { gameEngine.startFromNode(resumeNodeId) }
        }
    }

    /**
     * Reacts to the engine's [HandlerEffect.EnterCinematic]: extracts the linear body, publishes it
     * for `CinematicScreen`, and requests navigation. On an invalid cinematic, logs and best-effort
     * resumes normal traversal from the start marker's successor.
     */
    private fun enterCinematic(effect: HandlerEffect.EnterCinematic) {
        val graph = currentGraph
        if (graph == null) {
            logger.exception(IllegalStateException("EnterCinematic with no currentGraph")) {
                "Cannot enter cinematic: no graph loaded"
            }
            return
        }

        extractCinematicBody(graph, effect.startNodeId, effect.endNodeId).fold(
            onSuccess = { body ->
                val resumeNodeId = graph.singleSuccessor(effect.endNodeId)
                assert(resumeNodeId == null || graph.getNode(resumeNodeId) != null) {
                    "Cinematic resume node $resumeNodeId not found in ${graph.chapterCode}"
                }
                cinematicResumeNodeId = resumeNodeId
                if (body.isEmpty()) {
                    resumeFromCinematic()
                } else {
                    updateState { it.copy(cinematicBody = body, isCinematicActive = true) }
                    _navigateToCinematic.trySend(Unit)
                }
            },
            onFailure = { error ->
                logger.exception(error) {
                    "Invalid cinematic from ${effect.startNodeId}; skipping"
                }
                val fallback = graph.singleSuccessor(effect.startNodeId)
                cinematicResumeNodeId = null
                updateState { it.copy(cinematicBody = emptyList(), isCinematicActive = false) }
                if (fallback != null) {
                    viewModelScope.launch {
                        gameEngine.resume()
                        gameEngine.startFromNode(fallback)
                    }
                } else {
                    gameEngine.resume()
                }
            }
        )
    }

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private companion object {
        const val CHOICES_PREFS_FILE = "SUTOKO_CHOICES_DARK_MODE"
        const val CHOICES_DARK_MODE_KEY = "enabled"
    }
}


