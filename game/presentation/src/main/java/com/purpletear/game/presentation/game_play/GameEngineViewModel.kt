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
import com.purpletear.game.presentation.game_play.chapter.NextChapterController
import com.purpletear.game.presentation.game_play.cinematic.CinematicCoordinator
import com.purpletear.game.presentation.game_play.layout.RightSideLayoutResolver
import com.purpletear.game.presentation.game_play.pacing.AutoAdvanceController
import com.purpletear.game.presentation.game_play.pacing.TimingGate
import com.purpletear.game.presentation.game_play.preferences.ChoicesDarkModeStore
import com.purpletear.game.presentation.game_play.state.FakeNotificationUi
import com.purpletear.game.presentation.game_play.state.GameEngineStateUiMapper
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for game engine interaction.
 * Orchestrates the game session.
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

    private val _navigateToBuy = Channel<Unit>(Channel.BUFFERED)
    val navigateToBuy: Flow<Unit> = _navigateToBuy.receiveAsFlow()

    private val _navigateToExit = Channel<Unit>(Channel.BUFFERED)
    val navigateToExit: Flow<Unit> = _navigateToExit.receiveAsFlow()

    private val rightSideLayout =
        RightSideLayoutResolver(chapterRepository, logger) { ids, source ->
            Log.d("GameEngine", "rightSideCharacterIds=$ids (source: $source)")
        }

    private val choicesDarkModeStore = ChoicesDarkModeStore(context)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val nextChapter = NextChapterController(
        gameId = gameId,
        chapterRepository = chapterRepository,
        logger = logger,
        scope = viewModelScope,
        updateState = ::updateState
    )
    val navigateToNextChapter: Flow<String> = nextChapter.navigateToNextChapter

    private val cinematic = CinematicCoordinator(
        gameEngine = gameEngine,
        logger = logger,
        scope = viewModelScope,
        updateState = ::updateState
    )
    val navigateToCinematic: Flow<Unit> = cinematic.navigateToCinematic

    private val audio = GameAudioController(context, viewModelScope)
    private val autoAdvance =
        AutoAdvanceController(
            gameEngine,
            timingScheduler,
            storyAdvanceModeRepository,
            viewModelScope
        )
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
                isChoicesDarkMode = choicesDarkModeStore.read()
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
                launch {
                    rightSideLayout.observeRoomLayout(gameId, chapterCode)
                        .collect(::publishRightSideIds)
                }

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
                        publishRightSideIds(rightSideLayout.onGraphLoaded(graph))

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

    private fun publishRightSideIds(rightSideIds: Set<Int>) {
        if (_uiState.value.rightSideCharacterIds != rightSideIds) {
            updateState { it.copy(rightSideCharacterIds = rightSideIds) }
        }
    }

    private fun resetForNewPlay() {
        timingGate.reset()
        audio.releaseSessionSounds()
        nextChapter.reset()

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
        cinematic.onGraphLoaded(graph)

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
        cinematic.onGraphLoaded(graph)

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
        updateState { GameEngineStateUiMapper.map(it, engineState) }
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

            is HandlerEffect.PlaySound -> audio.playSound(
                effect.soundUrl,
                effect.loop,
                effect.volume
            )

            is HandlerEffect.PlayVocal -> audio.playVocal(effect.audioUrl)

            is HandlerEffect.StopSound -> audio.stopSound()

            is HandlerEffect.ChangeChapter -> {
                nextChapter.onChapterChange(effect.chapterCode)
            }

            is HandlerEffect.ShowChoices -> {
                updateState {
                    it.copy(
                        choices = effect.choices,
                        isChoicesRevealed = false
                    )
                }
            }

            is HandlerEffect.EnterCinematic -> cinematic.handle(effect)

            is HandlerEffect.ShowFakeNotification -> handleShowFakeNotification(effect)

            is HandlerEffect.ShowVisualNovel -> handleShowVisualNovel(effect)

            else -> {
                Log.d("GameEngine", "Received effect: ${effect::class.simpleName}")
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

    fun onVisualNovelDismissed() {
        if (_uiState.value.visualNovel == null) return
        updateState { it.copy(visualNovel = null) }
        audio.fadeOutVisualNovelSounds()
        audio.releaseVisualNovelDialogSounds()
        viewModelScope.launch { gameEngine.resumeFromVisualNovel() }
    }

    fun playVisualNovelDialogSound(path: String) = audio.playVisualNovelDialogSound(path)

    private fun handleChangeScene(effect: HandlerEffect.ChangeScene) {
        viewModelScope.launch {
            val scene = getSceneUseCase(effect.sceneId)
            this@GameEngineViewModel.updateState { it.copy(currentScene = scene) }
        }
    }

    fun onNextChapterClicked() {
        nextChapter.onNextChapterClicked(_uiState.value.isNextChapterAvailable)
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

    fun onMangaPageDismissed() {
        viewModelScope.launch {
            gameEngine.resumeFromMangaPage()
        }
    }

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
        choicesDarkModeStore.write(next)
    }

    /**
     * Loads a scene for the cinematic player.
     */
    suspend fun loadScene(sceneId: Int) = getSceneUseCase(sceneId)

    /**
     * Called by `CinematicScreen` when the cinematic body is exhausted (or cancelled). Resumes the
     * SMS engine at the node after `[intro=end]` and clears the cinematic slice.
     */
    fun onCinematicFinished() = cinematic.onCinematicFinished()

    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }
}
