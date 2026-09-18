package com.purpletear.game.presentation.game_preview

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.ToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GamePreviewViewModel.Companion.MAX_GRANT_CHECK_ATTEMPTS
import com.purpletear.game.presentation.game_preview.components.formatReleaseDate
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.handlers.GamePreviewPurchaseHandler
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.BuildConfig
import com.purpletear.sutoko.game.exception.DownloadAlreadyInProgressException
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.canAccessGameOptions
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.model.game.GameDownloadState
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.GamePreviewSoundRepository
import com.purpletear.sutoko.game.repository.UserRoleRepository
import com.purpletear.sutoko.game.repository.game.FavoriteGamesRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import com.purpletear.sutoko.game.usecase.PrepareGameLaunchUseCase
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.domain.repository.EntitlementRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val favoriteGamesRepository: FavoriteGamesRepository,
    private val chapterRepository: ChapterRepository,
    private val gameInstallRepository: GameInstallRepository,
    private val mediaUrlResolver: MediaUrlResolver,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val saveUserNickNameUseCase: SaveUserNickNameUseCase,
    private val prepareGameLaunchUseCase: PrepareGameLaunchUseCase,
    private val toastService: ToastService,
    private val restartGameUseCase: RestartGameUseCase,
    private val downloadGameUseCase: DownloadGameUseCase,
    private val purchaseHandler: GamePreviewPurchaseHandler,
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val soundRepository: GamePreviewSoundRepository,
    private val entitlementRepository: EntitlementRepository,
    private val shopRepository: ShopRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val logger: Logger,
) : ViewModel() {

    private val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

    init {
        GamePreviewLogger.i("LIFE") { "GamePreviewViewModel created for gameId=$gameId" }
    }

    private val currentChapterRefreshTicks = MutableStateFlow(0)
    private val observationRefreshTicks = MutableStateFlow(0)
    private val catalogLoadState = MutableStateFlow<GamePreviewUiState>(GamePreviewUiState.Loading)
    private val downloadRequested = MutableStateFlow(false)
    private val _isLoadingChapters = MutableStateFlow(true)
    val isLoadingChapters = _isLoadingChapters.asStateFlow()
    private var playNavigationPending = false

    fun onResume() {
        playNavigationPending = false
        currentChapterRefreshTicks.value += 1
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChapter: StateFlow<Chapter?> = currentChapterRefreshTicks
        .flatMapLatest {
            chapterRepository.observeCurrentChapter(gameId).catch { error ->
                logger.exception(error) { "Failed to observe current chapter for gameId=$gameId" }
                emit(null)
            }
        }
        .onEach { chapter ->
            GamePreviewLogger.d("OBS") {
                chapter?.let {
                    "currentChapter emitted: gameId=$gameId, code=${it.code}, number=${it.number}, available=${it.available}"
                } ?: "currentChapter emitted: null for gameId=$gameId"
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = null,
        )

    /**
     * Number of distinct released chapter numbers; alternatives count once.
     * Null while loading an empty local store, so no unverified catalog count is shown.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val releasedChaptersCount: StateFlow<Int?> = observationRefreshTicks.flatMapLatest {
        combine(
            chapterRepository.observeChapters(gameId),
            _isLoadingChapters,
        ) { chapters, loading ->
            if (chapters.isEmpty() && loading) null else chapters.asSequence()
                .filter { it.available }
                .map { it.number }
                .distinct()
                .count()
        }.catch { error ->
            logger.exception(error) { "Failed to observe released chapters for gameId=$gameId" }
            emit(null)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = null,
        )

    val isUserConnected: StateFlow<Boolean> = userRepository.observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = userRepository.isConnected().getOrDefault(false),
        )

    /** The story options entry point is only offered to the tester account. */
    val isOptionsVisible: StateFlow<Boolean> = userRepository.observeUser()
        .map { it.canAccessGameOptions() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    /** Administrators bypass chapter availability rules (unreleased chapters). */
    val isAdmin: StateFlow<Boolean> = userRoleRepository.observe()
        .map { it == UserRole.ADMINISTRATOR }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    /**
     * Set to true when the preview download fails: per backend contract, any
     * error on the preview entry point means "hide the feature" and must never
     * break the player flow.
     */
    private val previewFeatureHidden = MutableStateFlow(false)

    /** The "Download preview" button is only offered to admins. */
    val isPreviewVisible: StateFlow<Boolean> =
        combine(isAdmin, previewFeatureHidden) { admin, hidden -> admin && !hidden }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(7000),
                initialValue = false,
            )

    /**
     * Server-confirmed entitlement for this story's SKUs (billing purchase,
     * coin grant or premium). Fail-closed: false until the server confirms.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val isEntitled: StateFlow<Boolean> = observationRefreshTicks
        .flatMapLatest {
            gameRepository.observeGame(id = gameId)
                .flatMapLatest { catalog ->
                    if (catalog == null || catalog.skus.isEmpty()) flowOf(false)
                    else entitlementRepository.observeIsGranted(catalog.skus)
                }.catch { error ->
                    logger.exception(error) { "Failed to observe entitlement for gameId=$gameId" }
                    emit(false)
                }
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(7000), false)

    @Keep
    private data class GameObservation(
        val catalog: com.purpletear.sutoko.game.model.game.GameCatalog?,
        val install: com.purpletear.sutoko.game.model.game.GameInstall?,
        val downloadState: GameDownloadState?,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val game: StateFlow<GamePreviewUiState> = observationRefreshTicks.flatMapLatest { combine(
        combine(
            gameRepository.observeGame(id = gameId),
            gameInstallRepository.observeInstall(gameId = gameId),
            gameInstallRepository.observeDownloadState(gameId),
        ) { catalog, install, downloadState ->
            GameObservation(
                catalog = catalog,
                install = install,
                downloadState = downloadState,
            )
        },
        isEntitled,
        favoriteGamesRepository.observeFavoriteIds(),
        catalogLoadState,
    ) { observation, isEntitled, favoriteIds, loadState ->
        when {
            observation.catalog != null -> {
                GamePreviewLogger.d("OBS") {
                    "game emitted Data: gameId=$gameId, title=${observation.catalog.title}, " +
                            "chapters=${observation.catalog.chaptersCount}, " +
                            "isPurchased=$isEntitled, " +
                            "downloadState=${observation.downloadState}"
                }
                GamePreviewUiState.Data(
                    item = GameItem(
                        observation.catalog,
                        observation.install,
                        // Full access = server-confirmed entitlement (billing purchase, coin grant or premium).
                        isPurchased = isEntitled,
                        bannerUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.banner?.storagePath),
                        logoUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.logo?.storagePath),
                        menuBackgroundUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.menuBackground?.storagePath),
                        authorAvatarUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.author?.avatarUrl),
                        titleUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.title?.storagePath),
                        downloadState = observation.downloadState,
                        isFavorite = gameId in favoriteIds,
                    ),
                    gameCatalog = observation.catalog,
                )
            }

            else -> loadState
        }
    }.catch { error ->
        GamePreviewLogger.e("OBS", error) { "game observation failed for gameId=$gameId" }
        logger.exception(error) { "Failed to observe game state for gameId=$gameId" }
        emit(GamePreviewUiState.Error(GameUiError.Load))
    } }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = GamePreviewUiState.Loading,
    )

    val isPurchasing: StateFlow<Boolean> = purchaseHandler.isPurchasing
    val isPurchaseLoading: StateFlow<Boolean> = purchaseHandler.isPurchaseLoading

    val isUserPremium: StateFlow<Boolean> = entitlementRepository.observeHasPremium()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    /** Persisted preference: the story's menu ambience plays unless muted. */
    val isMenuSoundMuted: StateFlow<Boolean> = soundRepository.observeMuted()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    private val currentGameItem: GameItem?
        get() = (game.value as? GamePreviewUiState.Data)?.item

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val grantRefreshTicks = MutableStateFlow(0)
    private var purchaseJob: Job? = null
    private var downloadJob: Job? = null
    private var deleteJob: Job? = null
    private var navigationJob: Job? = null
    private var initialLoadStarted = false
    private val catalogLoadMutex = Mutex()

    private val _events = MutableSharedFlow<GamePreviewEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    val unlockFeedbackPending: StateFlow<Boolean> =
        savedStateHandle.getStateFlow("unlockFeedbackPending", false)

    fun onUnlockFeedbackShown() {
        savedStateHandle["unlockFeedbackPending"] = false
    }

    /**
     * Starts loading and observation once per ViewModel, including when the UI reattaches.
     * Explicit reloads use [refresh].
     */
    fun start() {
        if (initialLoadStarted) return
        GamePreviewLogger.i("LIFE") { "start() called for gameId=$gameId" }
        initialLoadStarted = true
        analyticsTracker.logEvent("story_preview_view", mapOf("story_id" to gameId))
        viewModelScope.launch {
            loadChapters()
        }
        viewModelScope.launch {
            recoverLostCatalog()
        }
        viewModelScope.launch {
            loadCatalog()
        }
        viewModelScope.launch {
            syncCoinPurchaseGrantOnDataLoad()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun recoverLostCatalog() {
        var wasPresent = false
        observationRefreshTicks.flatMapLatest {
            gameRepository.observeGame(gameId).catch { error ->
                logger.exception(error) { "Failed to observe catalog removal for gameId=$gameId" }
            }
        }.map { it != null }.distinctUntilChanged().collect { present ->
            val wasRemoved = wasPresent && !present
            wasPresent = present
            if (wasRemoved) loadCatalog(onlyIfMissing = true)
        }
    }

    private suspend fun loadCatalog(onlyIfMissing: Boolean = false) = catalogLoadMutex.withLock {
        try {
            val cached = gameRepository.observeGame(gameId).first()
            if (onlyIfMissing && cached != null) return@withLock
            catalogLoadState.value = GamePreviewUiState.Loading
            val result = if (cached == null) {
                gameRepository.getGameCatalog(gameId, Locale.getDefault().toLanguageTag())
            } else {
                gameRepository.refreshGameCatalog(gameId, Locale.getDefault().toLanguageTag())
            }
            catalogLoadState.value = result.fold(
                onSuccess = { catalog ->
                    if (catalog == null) GamePreviewUiState.NotFound else GamePreviewUiState.Loading
                },
                onFailure = { error ->
                    logger.exception(error) { "Failed to refresh catalog for gameId=$gameId" }
                    GamePreviewUiState.Error(GameUiError.Load)
                },
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            logger.exception(error) { "Failed to load catalog for gameId=$gameId" }
            catalogLoadState.value = GamePreviewUiState.Error(GameUiError.Load)
        }
    }

    fun onAction(action: GamePreviewAction) {
        when (action) {
            GamePreviewAction.OnBuy -> onBuy()
            GamePreviewAction.OnAbortBuy -> purchaseHandler.abortPurchaseFlow()
            GamePreviewAction.OnBuyConfirm -> onPurchase()
            GamePreviewAction.OnDownload -> onStartDownload()
            GamePreviewAction.OnUpdateGame -> onStartDownload()
            GamePreviewAction.OnDownloadPreview -> if (isAdmin.value) onStartDownload(preview = true)
            GamePreviewAction.OnUpdateApp -> sendEvent(GamePreviewEvent.OpenAppStore)
            GamePreviewAction.OnPlay -> onPlay()
            GamePreviewAction.OnTry -> onPlay(isTrial = true)
            GamePreviewAction.OnRestart -> sendEvent(GamePreviewEvent.ShowRestartDialog)
            GamePreviewAction.OnRestartConfirm -> onRestartGame()
            GamePreviewAction.OnDelete -> onDeleteGame()
            GamePreviewAction.OnToggleFavorite -> onToggleFavorite()
            GamePreviewAction.OnToggleMenuSound -> onToggleMenuSound()
        }
    }

    private fun onToggleMenuSound() {
        GamePreviewLogger.i("SND") { "onToggleMenuSound() gameId=$gameId" }
        viewModelScope.launch {
            soundRepository.setMuted(!isMenuSoundMuted.value)
        }
    }

    private fun onToggleFavorite() {
        GamePreviewLogger.i("FAV") { "onToggleFavorite() gameId=$gameId" }
        viewModelScope.launch {
            try {
                favoriteGamesRepository.toggle(gameId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                GamePreviewLogger.e("FAV", e) { "onToggleFavorite() failed for gameId=$gameId" }
                logger.exception(e) { "Toggle favorite failed for gameId=$gameId" }
            }
        }
    }

    /**
     * Re-fetches this story's catalog row and chapters from the network. The
     * Room observation flows update the UI automatically when fresh data lands.
     * An installed game is never evicted by the app-foreground catalog syncs
     * (see GameDao).
     */
    fun refresh() {
        if (!_isRefreshing.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                observationRefreshTicks.value += 1
                currentChapterRefreshTicks.value += 1
                grantRefreshTicks.value += 1
                loadCatalog()
                if (!loadChapters()) {
                    logger.warning(
                        message = "Preview refresh failed for gameId=$gameId",
                        data = mapOf("gameId" to gameId),
                    )
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private val _isSavingNickName = MutableStateFlow(false)
    val isSavingNickName = _isSavingNickName.asStateFlow()

    fun onNickNameConfirmed(name: String?, isTrial: Boolean) {
        if (!_isSavingNickName.compareAndSet(false, true)) return
        GamePreviewLogger.d("NAV") {
            "onNickNameConfirmed() gameId=$gameId, isTrial=$isTrial, name=${
                name?.take(
                    20
                )
            }"
        }
        viewModelScope.launch {
            try {
                val saveResult = saveUserNickNameUseCase(gameId, name)
                if (saveResult.isFailure) {
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.NickName))
                    return@launch
                }
                navigateToPlay(requestNickName = false, isTrial = isTrial)?.join()
            } finally {
                _isSavingNickName.value = false
            }
        }
    }

    private fun onBuy() {
        if (!isUserConnected.value) {
            GamePreviewLogger.d("PUR") { "onBuy() user not connected for gameId=$gameId" }
            sendEvent(GamePreviewEvent.OpenAccountConnection)
            return
        }
        viewModelScope.launch {
            // Local gate first: a loaded balance holding fewer coins than the
            // price never reaches the server - the user goes to the shop.
            if (lacksCoins(currentGameItem?.price ?: 0)) {
                GamePreviewLogger.i("PUR") { "onBuy() insufficient coins for gameId=$gameId" }
                onInsufficientFunds()
                return@launch
            }
            GamePreviewLogger.i("PUR") { "onBuy() starting purchase flow for gameId=$gameId" }
            purchaseHandler.startPurchaseFlow()
        }
    }

    /**
     * True only when the balance is loaded and holds fewer coins than [price].
     * An unloaded balance never blocks the flow: the server stays the fallback
     * arbiter (see [BuyStoryError.InsufficientFunds]).
     */
    private suspend fun lacksCoins(price: Int): Boolean {
        val balance = shopRepository.observeBalance().first()
        return balance.isLoaded() && balance.coins < price
    }

    private fun onInsufficientFunds() {
        toastService(R.string.game_presentation_insufficient_funds_alert_description)
        sendEvent(GamePreviewEvent.OpenShop)
    }

    /**
     * Single gatekeeper for starting the game, trial included. An unreleased
     * chapter no longer disables the Play button: tapping it surfaces the same
     * message as GamePreviewUnavailable as a toast instead. Administrators
     * bypass the check. A null chapter (not loaded yet) is a dead end: the
     * buttons are disabled in that state, and any programmatic attempt only
     * gets an error toast - never a navigation.
     */
    private fun onPlay(isTrial: Boolean = false) {
        if (playNavigationPending || navigationJob?.isActive == true || deleteJob?.isActive == true) return
        // Runtime compatibility gate: the button state (GameActionState.UpdateApp)
        // already hides Play/Try, but other entry points (deep links, chapter
        // screen) reach this method directly - never launch an unsupported story.
        val item = (game.value as? GamePreviewUiState.Data)?.item
        if (item != null && item.canvasTechnologyRequiredVersion > BuildConfig.CANVAS_VERSION_COMPATIBILITY) {
            GamePreviewLogger.w("NAV") {
                "onPlay() aborted: gameId=$gameId requires canvas " +
                        "v${item.canvasTechnologyRequiredVersion}, app supports v${BuildConfig.CANVAS_VERSION_COMPATIBILITY}"
            }
            sendEvent(GamePreviewEvent.OpenAppStore)
            return
        }
        val chapter = currentChapter.value
        when {
            chapter == null -> {
                GamePreviewLogger.w("NAV") { "onPlay() aborted: null currentChapter for gameId=$gameId" }
                sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
            }

            !chapter.available && !isAdmin.value -> {
                GamePreviewLogger.d("NAV") { "onPlay() chapter ${chapter.number} unavailable for gameId=$gameId" }
                toastService(
                    R.string.game_presentation_game_preview_next_chapter,
                    chapter.formatReleaseDate(),
                )
            }

            else -> {
                // Trial bypasses the install gate (Purchase shows Try regardless of
                // localVersion): without files on disk the chapter graph load fails.
                // Download the archive first, then auto-play on completion.
                if (isTrial && !isGameInstalled()) {
                    GamePreviewLogger.i("NAV") { "onPlay() trial without local files, downloading first for gameId=$gameId" }
                    onStartDownload(playTrialOnComplete = true)
                } else {
                    navigateToPlay(requestNickName = true, isTrial = isTrial)
                }
            }
        }
    }

    /** Trials must use the same current archive as full gameplay. */
    private fun isGameInstalled(): Boolean {
        val item = (game.value as? GamePreviewUiState.Data)?.item ?: return false
        return item.localVersion == item.version
    }

    private fun navigateToPlay(requestNickName: Boolean, isTrial: Boolean = false): Job? {
        if (playNavigationPending || navigationJob?.isActive == true) return null
        val data = game.value as? GamePreviewUiState.Data ?: run {
            GamePreviewLogger.w("NAV") { "navigateToPlay() ignored: no data for gameId=$gameId" }
            return null
        }
        navigationJob = viewModelScope.launch {
            // Boundary invariant: PlayGame requires a chapter downstream
            // (SmsGameActivity crashes without one), so never emit it without.
            val chapter = currentChapter.value ?: run {
                GamePreviewLogger.w("NAV") { "navigateToPlay() aborted: null currentChapter for gameId=$gameId" }
                logger.warning(
                    message = "Preview navigateToPlay() aborted with null currentChapter for gameId=$gameId",
                    data = mapOf("gameId" to gameId)
                )
                sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
                return@launch
            }
            if (!chapter.available && userRoleRepository.get() != UserRole.ADMINISTRATOR) {
                toastService(R.string.game_presentation_game_preview_next_chapter, chapter.formatReleaseDate())
                return@launch
            }
            val needsNickName = prepareGameLaunchUseCase(data.gameCatalog, requestNickName)
                .getOrElse { error ->
                    logger.exception(error) { "Could not prepare nickname for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.NickName))
                    return@launch
                }

            GamePreviewLogger.i("NAV") {
                "navigateToPlay() gameId=$gameId, isTrial=$isTrial, " +
                        "chapterCode=${chapter.normalizedCode}, needsNickName=$needsNickName"
            }

            if (needsNickName) {
                sendEvent(GamePreviewEvent.RequestNickName(isTrial = isTrial))
            } else {
                playNavigationPending = true
                if (isTrial) {
                    analyticsTracker.logEvent(
                        "trial_start",
                        mapOf(
                            "story_id" to gameId,
                            "chapter_code" to chapter.normalizedCode
                        )
                    )
                }
                sendEvent(
                    GamePreviewEvent.PlayGame(
                        gameId = gameId,
                        legacyId = data.gameCatalog.legacyId,
                        isPurchased = data.item.isPurchased,
                        chapterCode = chapter.normalizedCode,
                        isTrial = isTrial,
                    )
                )
            }
        }
        return navigationJob
    }

    private fun sendEvent(event: GamePreviewEvent) {
        GamePreviewLogger.d("LIFE") { "sendEvent() ${event::class.simpleName} for gameId=$gameId" }
        if (event is GamePreviewEvent.ShowError) {
            toastService(event.error.stringRes)
        }
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    override fun onCleared() {
        GamePreviewLogger.i("LIFE") { "GamePreviewViewModel cleared for gameId=$gameId" }
        super.onCleared()
    }

    private suspend fun loadChapters(): Boolean {
        _isLoadingChapters.value = true
        var success = true
        try {
            getChaptersUseCase(gameId).collect { result ->
                result.onSuccess { chapters ->
                    if (chapters.isEmpty()) {
                        logger.warning(
                            message = "Preview loaded empty chapter list for gameId=$gameId",
                            data = mapOf("gameId" to gameId),
                        )
                    }
                }.onFailure { error ->
                    success = false
                    logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            success = false
            logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
            sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
        } finally {
            _isLoadingChapters.value = false
        }
        return success
    }

    private data class GrantCheck(
        val userId: String,
        val skus: List<String>,
        val refresh: Int,
    )

    private suspend fun syncCoinPurchaseGrantOnDataLoad() {
        combine(game, userRepository.observeUser(), grantRefreshTicks) { state, user, refresh ->
            val data = state as? GamePreviewUiState.Data
            if (user == null || data == null || data.item.isPurchased || data.gameCatalog.skus.isEmpty()) {
                null
            } else {
                GrantCheck(user.id, data.gameCatalog.skus.distinct().sorted(), refresh)
            }
        }.distinctUntilChanged().collectLatest { check ->
            if (check != null) attemptCoinGrantCheck(check.skus)
        }
    }

    private suspend fun attemptCoinGrantCheck(skus: List<String>) {
        repeat(MAX_GRANT_CHECK_ATTEMPTS) { attempt ->
            val result = entitlementRepository.refreshGrant(skus)
            if (result.isSuccess) return
            if (attempt == MAX_GRANT_CHECK_ATTEMPTS - 1) {
                logger.warning(
                    message = "Coin purchase grant check gave up after $MAX_GRANT_CHECK_ATTEMPTS attempts for gameId=$gameId",
                    data = mapOf("gameId" to gameId),
                )
            } else {
                delay(GRANT_CHECK_RETRY_DELAY_MS * (attempt + 1))
            }
        }
    }

    private fun onStartDownload(playTrialOnComplete: Boolean = false, preview: Boolean = false) {
        if (downloadRequested.value || downloadJob?.isActive == true || deleteJob?.isActive == true) return
        downloadRequested.value = true
        downloadJob = viewModelScope.launch {
            try {
                if (preview && userRoleRepository.get() != UserRole.ADMINISTRATOR) return@launch
                downloadGameUseCase(gameId = gameId, preview = preview).collect { progress ->
                    GamePreviewLogger.d("DOWN") { "Download progress=$progress for gameId=$gameId" }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: DownloadAlreadyInProgressException) {
                return@launch
            } catch (error: Exception) {
                if (preview) {
                    onPreviewDownloadFailure(error)
                } else {
                    logger.exception(error) { "Download failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.fromDownloadError(error)))
                }
                return@launch
            } finally {
                downloadRequested.value = false
            }
            if (playTrialOnComplete) {
                navigateToPlay(requestNickName = true, isTrial = true)
            }
        }
    }

    /**
     * Only a definitive server "no" ([GameUiError.DownloadForbidden], HTTP 403)
     * hides the feature, per backend contract. Transient failures (network,
     * 5xx, user token not loaded yet) keep the button alive and surface an
     * error toast, so a retry is one tap away - hiding on a flaky call made
     * the feature look dead.
     */
    private fun onPreviewDownloadFailure(error: Throwable) {
        GamePreviewLogger.e("DOWN", error) { "preview download failed for gameId=$gameId" }
        logger.exception(error) { "Preview download failed for gameId=$gameId" }
        val uiError = GameUiError.fromDownloadError(error)
        if (uiError == GameUiError.DownloadForbidden) {
            previewFeatureHidden.value = true
        } else {
            sendEvent(GamePreviewEvent.ShowError(uiError))
        }
    }

    private fun onDeleteGame() {
        if (downloadRequested.value || downloadJob?.isActive == true || deleteJob?.isActive == true) return
        val legacyId = currentGameItem?.legacyId
        deleteJob = viewModelScope.launch {
            try {
                gameInstallRepository.deleteGame(gameId, legacyId).getOrThrow()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logger.exception(error) { "Delete failed for gameId=$gameId" }
                sendEvent(GamePreviewEvent.ShowError(GameUiError.Delete))
            }
        }
    }

    private fun onPurchase() {
        if (purchaseJob?.isActive == true || !purchaseHandler.isPurchasing.value) return

        val sku = currentGameItem?.skuIdentifiers?.firstOrNull()
        if (sku == null) {
            GamePreviewLogger.w("PUR") { "onPurchase() no SKU for gameId=$gameId" }
            logger.warning("No SKU available for purchase for gameId=$gameId")
            purchaseHandler.abortPurchaseFlow()
            sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
            return
        }

        GamePreviewLogger.i("PUR") { "onPurchase() confirming sku=$sku for gameId=$gameId" }
        analyticsTracker.logEvent(
            "purchase_initiated",
            mapOf("sku" to sku, "method" to "coins", "story_id" to gameId)
        )
        purchaseJob = viewModelScope.launch {
            // The balance may have dropped while the confirmation dialog was
            // open: re-check before hitting the server.
            if (lacksCoins(currentGameItem?.price ?: 0)) {
                GamePreviewLogger.i("PUR") { "onPurchase() insufficient coins for sku=$sku" }
                purchaseHandler.abortPurchaseFlow()
                onInsufficientFunds()
                return@launch
            }
            purchaseHandler.confirmPurchase(sku)
                .onSuccess {
                    GamePreviewLogger.i("PUR") { "onPurchase() succeeded for sku=$sku" }
                    analyticsTracker.logEvent(
                        "purchase_completed",
                        mapOf("sku" to sku, "method" to "coins", "story_id" to gameId)
                    )
                    savedStateHandle["unlockFeedbackPending"] = true
                    sendEvent(GamePreviewEvent.PurchaseSuccess)
                }
                .onFailure { error ->
                    GamePreviewLogger.e("PUR", error) { "onPurchase() failed for sku=$sku" }
                    analyticsTracker.logEvent(
                        "purchase_failed",
                        mapOf(
                            "sku" to sku,
                            "method" to "coins",
                            "story_id" to gameId,
                            "error" to error::class.simpleName.orEmpty()
                        )
                    )
                    logger.exception(error) { "Purchase failed for sku=$sku" }
                    when (error) {
                        is BuyStoryError.AlreadyOwned -> sendEvent(GamePreviewEvent.ShowAlreadyBoughtAlert)
                        is BuyStoryError.InsufficientFunds -> onInsufficientFunds()
                        is BuyStoryError.NotPurchasable -> sendEvent(
                            GamePreviewEvent.ShowError(
                                GameUiError.Purchase
                            )
                        )

                        else -> sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
                    }
                }
        }
    }

    private fun onRestartGame() {
        GamePreviewLogger.i("LIFE") { "onRestartGame() gameId=$gameId" }
        viewModelScope.launch {
            restartGameUseCase(gameId, legacyId = currentGameItem?.legacyId)
                .onSuccess {
                    GamePreviewLogger.i("LIFE") { "onRestartGame() succeeded for gameId=$gameId" }
                    currentChapterRefreshTicks.value += 1
                    toastService(R.string.game_presentation_game_restart_success)
                }
                .onFailure { error ->
                    GamePreviewLogger.e(
                        "LIFE",
                        error
                    ) { "onRestartGame() failed for gameId=$gameId" }
                    logger.exception(error) { "Restart failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Restart))
                }
        }
    }

    private companion object {
        const val MAX_GRANT_CHECK_ATTEMPTS = 3
        const val GRANT_CHECK_RETRY_DELAY_MS = 1_000L
    }
}
