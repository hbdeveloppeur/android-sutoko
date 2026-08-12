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
import com.purpletear.game.presentation.game_play.state.VisualNovelUi
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.engine.GameEngine
import com.purpletear.sutoko.game.engine.GameEngineState
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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
    private val storyAdvanceModeRepository: StoryAdvanceModeRepository,
    private val makeToastService: MakeToastService,
    private val analyticsTracker: AnalyticsTracker,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var typingPlayer: MediaPlayer? = null

    /** Ambient/looping channel: a new looping sound replaces the previous one. */
    private var soundPlayer: MediaPlayer? = null

    /** Non-looping sounds: each one gets its own player so effects can overlap. */
    private val oneShotSoundPlayers = mutableSetOf<MediaPlayer>()

    /** Visual novel sounds: one player per authored sound, all fading out together on dismiss. */
    private val visualNovelChannels = mutableListOf<VisualNovelChannel>()
    private var visualNovelFadeJob: Job? = null

    /** Visual novel dialog sounds: one-shots fired by the overlay as each dialog appears. */
    private val visualNovelDialogPlayers = mutableSetOf<MediaPlayer>()

    private data class VisualNovelChannel(val player: MediaPlayer, val volume: Float)

    private var vocalPlayer: MediaPlayer? = null
    private var vocalProgressJob: Job? = null

    private var isFingerHeld = false
    private var isImageViewerOpen = false

    /** Pending auto-advance past the current tap gate; cancelled as soon as the gate closes. */
    private var autoAdvanceJob: Job? = null

    /** Whether the story advances on its own past tap gates or waits for a player tap. */
    private val advanceMode: StateFlow<StoryAdvanceMode> = storyAdvanceModeRepository.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, StoryAdvanceMode.AUTO_PLAY)

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
                launch { observeAdvanceMode() }
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
        timingScheduler.setHoldPaused(false)
        typingPlayer?.release()
        typingPlayer = null

        soundPlayer?.stop()
        soundPlayer?.release()
        soundPlayer = null
        releaseOneShotSounds()

        isFingerHeld = false
        isImageViewerOpen = false

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
                isAwaitingTap = false,
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
        if (engineState is GameEngineState.ChapterFinished) {
            logChapterFinished(engineState.chapterCode)
        }
        if (engineState is GameEngineState.AwaitingTap && shouldAutoAdvance(engineState)) {
            scheduleAutoAdvance(engineState)
        } else {
            autoAdvanceJob?.cancel()
        }
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

    /**
     * Whether a parked tap gate resolves on its own: always in auto-play mode, and for
     * gates that declared [GameEngineState.AwaitingTap.requiresTap] = false (e.g. scene
     * transitions), which auto-continue even in click-to-advance mode.
     */
    private fun shouldAutoAdvance(state: GameEngineState.AwaitingTap): Boolean =
        advanceMode.value == StoryAdvanceMode.AUTO_PLAY || !state.requiresTap

    /**
     * Applies a mid-game [StoryAdvanceMode] change to a currently parked tap gate: turning
     * AutoPlay off cancels the pending advance (unless the gate never requires a tap);
     * turning it on schedules one if the engine is still waiting for a tap.
     */
    private suspend fun observeAdvanceMode() {
        advanceMode.collect {
            val state = gameEngine.state.value
            if (state is GameEngineState.AwaitingTap && shouldAutoAdvance(state)) {
                scheduleAutoAdvance(state)
            } else {
                autoAdvanceJob?.cancel()
            }
        }
    }

    /**
     * Auto-advance driver: resumes the engine once the tap gate's pacing delay has elapsed,
     * so the story progresses without requiring a tap. A player tap before the deadline wins:
     * the state leaves AwaitingTap, this job is cancelled, and [GameEngine.advanceOnTap]
     * no-ops otherwise. Uses the timing scheduler so hold-to-pause freezes the countdown too.
     */
    private fun scheduleAutoAdvance(state: GameEngineState.AwaitingTap) {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            timingScheduler.delay(state.autoAdvanceAfterMs)
            val current = gameEngine.state.value
            if (current is GameEngineState.AwaitingTap && current.currentNodeId == state.currentNodeId) {
                gameEngine.advanceOnTap()
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

            is HandlerEffect.PlayTypingSound -> playTypingSound()

            is HandlerEffect.PlaySound -> playSound(effect.soundUrl, effect.loop, effect.volume)

            is HandlerEffect.PlayVocal -> playVocal(effect.audioUrl)

            is HandlerEffect.StopSound -> stopSound()

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
        releaseOneShotSounds()
        releaseVisualNovelSounds()
        releaseVisualNovelDialogSounds()
        vocalPlayer?.release()
        vocalPlayer = null
        vocalProgressJob?.cancel()
        Trace.endSection()
        super.onCleared()
    }

    private fun playSound(soundUrl: String, loop: Boolean, volume: Float) {
        if (loop) {
            playLoopingSound(soundUrl, volume)
        } else {
            playOneShotSound(soundUrl, volume)
        }
    }

    private fun playLoopingSound(soundUrl: String, volume: Float) {
        soundPlayer?.release()
        soundPlayer = try {
            MediaPlayer().apply {
                setDataSource(soundUrl)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play sound: $soundUrl", e)
            null
        }
    }

    /**
     * Fire-and-forget playback: every one-shot sound owns its player, so several
     * effects can overlap each other and the ambient loop. The player removes and
     * releases itself on completion; [releaseOneShotSounds] covers early teardown.
     */
    private fun playOneShotSound(soundUrl: String, volume: Float) {
        val player = try {
            MediaPlayer().apply {
                setDataSource(soundUrl)
                setVolume(volume, volume)
                prepare()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play sound: $soundUrl", e)
            return
        }
        oneShotSoundPlayers += player
        player.setOnCompletionListener { mp ->
            oneShotSoundPlayers.remove(mp)
            mp.release()
        }
        player.start()
    }

    private fun releaseOneShotSounds() {
        oneShotSoundPlayers.forEach {
            it.setOnCompletionListener(null)
            it.release()
        }
        oneShotSoundPlayers.clear()
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
        releaseOneShotSounds()
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
        playVisualNovelSounds(effect.sounds)
    }

    /**
     * Called when the player dismisses the visual novel overlay (dismiss button or back).
     * Idempotent: clears the UI state first, fades the sounds out, then resumes the engine
     * (which no-ops when it is not parked on a visual novel node).
     */
    fun onVisualNovelDismissed() {
        if (_uiState.value.visualNovel == null) return
        updateState { it.copy(visualNovel = null) }
        fadeOutVisualNovelSounds()
        releaseVisualNovelDialogSounds()
        viewModelScope.launch { gameEngine.resumeFromVisualNovel() }
    }

    /**
     * Plays the one-shot sound attached to a visual novel dialog (called by the overlay when
     * the dialog appears). Fire-and-forget like [playOneShotSound]; the path is a local file
     * bundled in the story's `assets/` directory, prepared asynchronously to stay non-blocking.
     */
    fun playVisualNovelDialogSound(path: String) {
        if (path.isBlank()) return
        val player = try {
            MediaPlayer().apply {
                setDataSource(path)
                setOnPreparedListener { it.start() }
                setOnCompletionListener { mp ->
                    visualNovelDialogPlayers.remove(mp)
                    mp.release()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("GameEngine", "Failed to play visual novel dialog sound: $path (what=$what extra=$extra)")
                    visualNovelDialogPlayers.remove(mp)
                    mp.release()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play visual novel dialog sound: $path", e)
            return
        }
        visualNovelDialogPlayers += player
    }

    private fun releaseVisualNovelDialogSounds() {
        visualNovelDialogPlayers.forEach { player ->
            player.setOnPreparedListener(null)
            player.setOnCompletionListener(null)
            player.setOnErrorListener(null)
            runCatching { player.stop() }
            player.release()
        }
        visualNovelDialogPlayers.clear()
    }

    /**
     * Every authored sound owns its player, so channels overlap freely and keep their own
     * volume/loop settings. [fadeOutVisualNovelSounds] / [releaseVisualNovelSounds] cover teardown.
     */
    private fun playVisualNovelSounds(sounds: List<Node.VisualNovel.Sound>) {
        releaseVisualNovelSounds()
        sounds.forEach { sound ->
            if (sound.path.isBlank()) return@forEach
            val player = try {
                MediaPlayer().apply {
                    setDataSource(sound.path)
                    isLooping = sound.loop
                    setVolume(sound.volume, sound.volume)
                    // Async prepare keeps the UI free even though the path is a local file
                    // bundled in the story's assets/ directory.
                    setOnPreparedListener { it.start() }
                    setOnErrorListener { mp, what, extra ->
                        Log.e("GameEngine", "Failed to play visual novel sound: ${sound.path} (what=$what extra=$extra)")
                        mp.release()
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("GameEngine", "Failed to play visual novel sound: ${sound.path}", e)
                null
            } ?: return@forEach
            visualNovelChannels += VisualNovelChannel(player, sound.volume)
        }
    }

    /** Stepped volume ramp to silence, then stop/release (GamePreviewMenuSoundEffect pattern). */
    private fun fadeOutVisualNovelSounds() {
        visualNovelFadeJob?.cancel()
        val channels = visualNovelChannels.toList()
        visualNovelChannels.clear()
        if (channels.isEmpty()) return
        visualNovelFadeJob = viewModelScope.launch {
            val steps = (VISUAL_NOVEL_FADE_MS / VISUAL_NOVEL_FADE_STEP_MS).toInt()
            repeat(steps) { step ->
                val scale = 1f - (step + 1).toFloat() / steps
                channels.forEach { channel ->
                    runCatching {
                        val volume = channel.volume * scale
                        channel.player.setVolume(volume, volume)
                    }
                }
                delay(VISUAL_NOVEL_FADE_STEP_MS)
            }
            channels.forEach { channel ->
                runCatching { channel.player.stop() }
                channel.player.release()
            }
        }
    }

    private fun releaseVisualNovelSounds() {
        visualNovelFadeJob?.cancel()
        visualNovelFadeJob = null
        visualNovelChannels.forEach { channel ->
            runCatching { channel.player.stop() }
            channel.player.release()
        }
        visualNovelChannels.clear()
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
     * screen, and resumes on release. The freeze happens inside the timing scheduler, so
     * in-flight scripts are never dropped (unlike GameEngine.pause()). Ignored while the
     * engine is not actively streaming messages (choices, cinematic, manga page).
     */
    fun onHoldPauseChanged(held: Boolean) {
        val state = _uiState.value
        if (isFingerHeld == held) return
        if (held && (state.isAwaitingInput || state.isCinematicActive || state.isMangaActive || state.visualNovel != null)) return
        isFingerHeld = held
        applyTimingGate()
    }

    /**
     * Image viewer: freezes the engine's pacing while a message image or avatar is
     * open fullscreen, and resumes on dismiss. Uses the same timing gate as
     * hold-to-pause, so an in-flight delay keeps its remaining time instead of
     * finishing behind the viewer. The two pause sources are combined, so lifting
     * the finger does not resume the story while the viewer is still open.
     */
    fun onImageViewerVisibilityChanged(visible: Boolean) {
        if (isImageViewerOpen == visible) return
        isImageViewerOpen = visible
        applyTimingGate()
    }

    private fun applyTimingGate() {
        val paused = isFingerHeld || isImageViewerOpen
        timingScheduler.setHoldPaused(paused)
        updateState { it.copy(isHoldPaused = paused) }
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
        const val VISUAL_NOVEL_FADE_MS = 600L
        const val VISUAL_NOVEL_FADE_STEP_MS = 50L
    }
}


