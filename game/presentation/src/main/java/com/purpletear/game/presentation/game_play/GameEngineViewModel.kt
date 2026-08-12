package com.purpletear.game.presentation.game_play

import android.content.Context
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
import com.purpletear.game.presentation.game_play.audio.GameAudioController
import com.purpletear.game.presentation.game_play.pacing.AutoAdvanceController
import com.purpletear.game.presentation.game_play.pacing.TimingGate
import com.purpletear.game.presentation.game_play.state.FakeNotificationUi
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.game.presentation.game_play.state.VisualNovelUi
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
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
import com.purpletear.sutoko.game.repository.StoryAdvanceModeRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.GetSceneUseCase
import com.purpletear.sutoko.game.usecase.LoadChapterGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
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
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for game engine interaction.
 * Orchestrates the game session: chapter bootstrap, engine-state → UI-state mapping, effect
 * dispatch and navigation. Media playback lives in [GameAudioController], auto-advance
 * pacing in [AutoAdvanceController], and manual pacing freezes in [TimingGate].
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
    private val storyAdvanceModeRepository: StoryAdvanceModeRepository,
    private val makeToastService: MakeToastService,
    private val analyticsTracker: AnalyticsTracker,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val gameId: String = checkNotNull(savedStateHandle["gameId"]) {
        "gameId is required"
    }
    private val chapterCode: String = checkNotNull(savedStateHandle["chapterCode"]) {
        "chapterCode is required"
    }
    private val isTrial: Boolean =
        savedStateHandle.get<Boolean>(SmsGameRoutes.IS_TRIAL_ARG) ?: false
    private val autoPlay: Boolean =
        savedStateHandle.get<Boolean>(SmsGameRoutes.AUTO_PLAY_ARG) ?: false

    /** Exposed so the cinematic screen can auto-skip when Kimi-cli is driving. */
    val isAutoPlay: Boolean get() = BuildConfig.DEBUG && autoPlay

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

    // Right-side layout sources: the chapter archive's layout.json wins when it declares
    // sides; the Room/API value is the fallback for archives without a layout.json.
    private var archiveRightSideIds: Set<Int>? = null
    private var roomRightSideIds: Set<Int> = emptySet()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Session controllers: plain classes on the ViewModel's scope, so their jobs and
    // players die with the ViewModel. Declared after _uiState: their callbacks write to it.
    private val audio = GameAudioController(context, viewModelScope)
    private val autoAdvance =
        AutoAdvanceController(gameEngine, timingScheduler, storyAdvanceModeRepository, viewModelScope)
    private val timingGate = TimingGate(timingScheduler) { paused ->
        updateState { it.copy(isHoldPaused = paused) }
    }

    init {
        Trace.beginSection("GameEngineViewModel.init")
        // The scheduler is a process-wide @Singleton: never inherit a stale hold from a
        // previous session.
        timingGate.reset()
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
                autoAdvance.start()
                launch { gameEngine.messages.collect { updateMessages(it) } }
                launch { gameEngine.effects.collect { handleEffect(it) } }
                launch {
                    audio.vocal.collect { vocal ->
                        updateState {
                            it.copy(
                                currentVocalUrl = vocal.url,
                                isVocalPlaying = vocal.isPlaying,
                                vocalProgress = vocal.progress
                            )
                        }
                    }
                }
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
                launch { observeChapterLayout(gameId, chapterCode) }

                loadChapterGraphAndStartGame(gameId, chapterCode)

                preloadScenes.join()
                preloadCharacters.join()

                val characters = characterRepository.getAll().associateBy { it.id }
                updateState {
                    it.copy(characters = characters)
                }

                if (BuildConfig.DEBUG && autoPlay) {
                    StoryAutoPlayer(uiState, this@GameEngineViewModel).start()
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
                        archiveRightSideIds = graph.rightSideCharacterIds.toSet()
                        publishRightSideIds()

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

    /**
     * Publishes the right-side character ids declared by the current chapter's layout.
     * The archive's `layout.json` is authoritative when it declares sides; otherwise the
     * Room/API value (`layout.sides.right`) is used. Emits an empty set when neither
     * declares a layout: the screen then falls back to the legacy main-character rule.
     */
    private fun publishRightSideIds() {
        val archiveIds = archiveRightSideIds?.takeIf { it.isNotEmpty() }
        val effective = archiveIds ?: roomRightSideIds
        val source = when {
            archiveIds != null -> "archive layout.json"
            roomRightSideIds.isNotEmpty() -> "api/room"
            else -> "none (legacy main-character rule)"
        }
        Log.d("GameEngine", "rightSideCharacterIds=$effective (source: $source)")
        if (_uiState.value.rightSideCharacterIds != effective) {
            updateState { it.copy(rightSideCharacterIds = effective) }
        }
    }

    private suspend fun observeChapterLayout(gameId: String, chapterCode: String) {
        chapterRepository.observeChapters(gameId)
            .catch { e ->
                logger.exception(e) { "chapter layout observation failed" }
                emit(emptyList())
            }
            .collect { chapters ->
                roomRightSideIds = chapters
                    .firstOrNull { it.normalizedCode == chapterCode.lowercase() }
                    ?.rightSideCharacterIds
                    .orEmpty()
                    .toSet()
                publishRightSideIds()
            }
    }

    private fun resetForNewPlay() {
        timingGate.reset()
        audio.releaseSessionSounds()
        pendingChapterCode = null

        updateState {
            it.copy(
                messages = emptyList(),
                choices = emptyList(),
                isChoicesRevealed = false,
                isAwaitingInput = false,
                isAwaitingTap = false,
                currentScene = null
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
        if (engineState is GameEngineState.ChapterFinished) {
            logChapterFinished(engineState.chapterCode)
        }
        autoAdvance.onEngineState(engineState)
        when (engineState) {
            is GameEngineState.AwaitingInput -> {
                updateState { it.copy(isAwaitingInput = true, isAwaitingTap = false) }
            }

            is GameEngineState.AwaitingTap -> {
                updateState { it.copy(isAwaitingTap = true, isAwaitingInput = false) }
            }

            is GameEngineState.AwaitingMangaDismissal -> {
                updateState { it.copy(isMangaActive = true) }
            }

            is GameEngineState.AwaitingVisualNovelDismissal -> {
                // The overlay is driven by the ShowVisualNovel effect; nothing to flag here.
                updateState { it.copy(isAwaitingInput = false, isAwaitingTap = false) }
            }

            is GameEngineState.Playing -> {
                updateState {
                    it.copy(
                        isAwaitingInput = false,
                        isAwaitingTap = false,
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
                        isAwaitingTap = false,
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
                        isAwaitingTap = false,
                        choices = emptyList(),
                        isChoicesRevealed = false,
                        isMangaActive = false
                    )
                }
            }
        }
    }

    private var lastLoggedFinishedChapter: String? = null

    /**
     * Analytics: one `trial_end` / `chapter_complete` per finished chapter. The
     * engine state is a StateFlow, so identical re-emissions are conflated; the
     * guard covers replay-then-finish-again of the same chapter.
     */
    private fun logChapterFinished(finishedChapterCode: String) {
        if (lastLoggedFinishedChapter == finishedChapterCode) return
        lastLoggedFinishedChapter = finishedChapterCode
        val event = if (isTrial) "trial_end" else "chapter_complete"
        analyticsTracker.logEvent(
            event,
            mapOf("story_id" to gameId, "chapter_code" to finishedChapterCode)
        )
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

            is HandlerEffect.PlayTypingSound -> audio.playTypingSound()

            is HandlerEffect.PlaySound -> audio.playSound(effect.soundUrl, effect.loop, effect.volume)

            is HandlerEffect.PlayVocal -> audio.playVocal(effect.audioUrl)

            is HandlerEffect.StopSound -> audio.stopSound()

            is HandlerEffect.ChangeChapter -> {
                pendingChapterCode = effect.chapterCode
                updateState { it.copy(isNextChapterAvailabilityResolved = false) }
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

            is HandlerEffect.ShowVisualNovel -> handleShowVisualNovel(effect)

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
                    isNextChapterAvailabilityResolved = true,
                    nextChapterReleaseDate = next?.releaseDate?.takeIf { date -> date > 0 },
                )
            }
        }
    }

    override fun onCleared() {
        Trace.beginSection("GameEngineViewModel.onCleared")
        timingGate.reset()
        audio.releaseAll()
        Trace.endSection()
        super.onCleared()
    }

    fun onVocalClicked(audioUrl: String) = audio.toggleVocal(audioUrl)

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

    private fun handleShowVisualNovel(effect: HandlerEffect.ShowVisualNovel) {
        updateState {
            it.copy(
                visualNovel = VisualNovelUi(
                    title = effect.title,
                    layers = effect.layers,
                    dialogs = effect.dialogs,
                    themeColorHex = effect.theme.colorHex,
                    themeOpacity = effect.theme.opacity,
                )
            )
        }
        audio.playVisualNovelSounds(effect.sounds)
    }

    /**
     * Called when the player dismisses the visual novel overlay (dismiss button or back).
     * Idempotent: clears the UI state first, fades the sounds out, then resumes the engine
     * (which no-ops when it is not parked on a visual novel node).
     */
    fun onVisualNovelDismissed() {
        if (_uiState.value.visualNovel == null) return
        updateState { it.copy(visualNovel = null) }
        audio.fadeOutVisualNovelSounds()
        audio.releaseVisualNovelDialogSounds()
        viewModelScope.launch { gameEngine.resumeFromVisualNovel() }
    }

    /**
     * Plays the one-shot sound attached to a visual novel dialog (called by the overlay when
     * the dialog appears).
     */
    fun playVisualNovelDialogSound(path: String) = audio.playVisualNovelDialogSound(path)

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
        // Ignore taps on stale choices (double-tap or tap during the choices fade-out),
        // and clear the choices synchronously so a second tap finds nothing to submit.
        val state = _uiState.value
        if (!state.isAwaitingInput || state.choices.none { it.nextNodeId == nextNodeId }) return
        updateState { it.copy(choices = emptyList()) }
        viewModelScope.launch {
            gameEngine.submitChoice(nextNodeId)
        }
    }

    /**
     * Called when the player taps a non-interactive area of the screen to advance the story.
     * While choices are pending, the tap opens the choice box instead, so the player is not
     * forced to hit the MakeAChoiceButton. Scrolls, button taps and overlay taps consume the
     * gesture before it reaches here. The engine no-ops when it is not parked for a tap.
     */
    fun onAdvanceOnTap() {
        val state = _uiState.value
        if (state.isAwaitingInput) {
            onRevealChoicesClicked()
            return
        }
        if (!state.isAwaitingTap) return
        viewModelScope.launch {
            gameEngine.advanceOnTap()
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
     * screen, and resumes on release. Ignored while the engine is not actively streaming
     * messages (choices, cinematic, manga page, visual novel).
     */
    fun onHoldPauseChanged(held: Boolean) {
        val state = _uiState.value
        if (timingGate.isFingerHeld == held) return
        if (held && (state.isAwaitingInput || state.isCinematicActive || state.isMangaActive || state.visualNovel != null)) return
        timingGate.setFingerHeld(held)
    }

    /**
     * Image viewer: freezes the engine's pacing while a message image or avatar is
     * open fullscreen, and resumes on dismiss. Shares the timing gate with hold-to-pause.
     */
    fun onImageViewerVisibilityChanged(visible: Boolean) {
        timingGate.setImageViewerOpen(visible)
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
