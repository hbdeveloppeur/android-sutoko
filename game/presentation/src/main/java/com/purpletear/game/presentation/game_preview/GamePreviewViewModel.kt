package com.purpletear.game.presentation.game_preview

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
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.UserRoleRepository
import com.purpletear.sutoko.game.repository.game.FavoriteGamesRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.domain.usecase.IsStoryGrantedUseCase
import com.purpletear.sutoko.shop.domain.usecase.ObserveCoinPurchasedSkusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val favoriteGamesRepository: FavoriteGamesRepository,
    private val chapterRepository: ChapterRepository,
    private val gameInstallRepository: GameInstallRepository,
    private val gamePurchaseRepository: PurchaseRepository,
    private val mediaUrlResolver: MediaUrlResolver,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val saveUserNickNameUseCase: SaveUserNickNameUseCase,
    private val toastService: ToastService,
    private val restartGameUseCase: RestartGameUseCase,
    private val downloadGameUseCase: DownloadGameUseCase,
    private val purchaseHandler: GamePreviewPurchaseHandler,
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val observeCoinPurchasedSkusUseCase: ObserveCoinPurchasedSkusUseCase,
    private val isStoryGrantedUseCase: IsStoryGrantedUseCase,
    private val logger: Logger,
    appVersionProvider: AppVersionProvider,
) : ViewModel() {

    private val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

    init {
        GamePreviewLogger.i("LIFE") { "GamePreviewViewModel created for gameId=$gameId" }
    }

    val appBuildNumber: Int = appVersionProvider.getVersionCode()

    /**
     * Bump to re-collect the current chapter. Friendzoned games persist their
     * own progress outside Room, so only a re-collection re-reads it (standard
     * games simply re-emit the same Room value). Bumped on ON_RESUME (the game
     * may have advanced while this screen sat in the back stack) and after a
     * confirmed restart (progress was just wiped, no lifecycle event fires).
     */
    private val currentChapterRefreshTicks = MutableStateFlow(0)

    fun onResume() {
        currentChapterRefreshTicks.value += 1
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChapter: StateFlow<Chapter?> = currentChapterRefreshTicks
        .flatMapLatest { chapterRepository.observeCurrentChapter(gameId) }
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
     * Number of released chapters actually stored locally. The catalog's
     * chaptersCount is a server-side cached value that can be stale, so the
     * real chapters win once loaded. Null until the local store holds at
     * least one chapter: callers then fall back to the catalog count.
     */
    val releasedChaptersCount: StateFlow<Int?> = chapterRepository.observeChapters(gameId)
        .map { chapters -> chapters.takeIf { it.isNotEmpty() }?.count { it.available } }
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
        .map { it?.id == OPTIONS_ACCESS_UID }
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

    private data class GameObservation(
        val catalog: com.purpletear.sutoko.game.model.game.GameCatalog?,
        val install: com.purpletear.sutoko.game.model.game.GameInstall?,
        val purchasedSkus: Set<String>,
        val hasGlobalPremium: Boolean,
        val downloadProgress: Float?,
    )

    val game: StateFlow<GamePreviewUiState> = combine(
        combine(
            gameRepository.observeGame(id = gameId),
            gameInstallRepository.observeInstall(gameId = gameId),
            gamePurchaseRepository.observePurchasedSkus(),
            gamePurchaseRepository.observeHasGlobalPremium(),
            gameInstallRepository.observeDownloadProgress(gameId),
        ) { catalog, install, purchasedSkus, hasGlobalPremium, downloadProgress ->
            GameObservation(
                catalog = catalog,
                install = install,
                purchasedSkus = purchasedSkus,
                hasGlobalPremium = hasGlobalPremium,
                downloadProgress = downloadProgress,
            )
        },
        observeCoinPurchasedSkusUseCase(),
        favoriteGamesRepository.observeFavoriteIds(),
    ) { observation, coinPurchasedSkus, favoriteIds ->
        when {
            observation.catalog != null -> {
                GamePreviewLogger.d("OBS") {
                    "game emitted Data: gameId=$gameId, title=${observation.catalog.title}, " +
                            "chapters=${observation.catalog.chaptersCount}, " +
                            "isPurchased=${observation.catalog.skus.any { it in observation.purchasedSkus || it in coinPurchasedSkus } || observation.hasGlobalPremium}, " +
                            "downloadProgress=${observation.downloadProgress}"
                }
                GamePreviewUiState.Data(
                    item = GameItem(
                        observation.catalog,
                        observation.install,
                        // Full access = owns a game SKU OR has an active global premium.
                        isPurchased = observation.catalog.skus.any { it in observation.purchasedSkus || it in coinPurchasedSkus } || observation.hasGlobalPremium,
                        bannerUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.banner?.storagePath),
                        logoUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.logo?.storagePath),
                        menuBackgroundUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.menuBackground?.storagePath),
                        authorAvatarUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.author?.avatarUrl),
                        titleUrl = mediaUrlResolver.resolveBannerUrl(observation.catalog.title?.storagePath),
                        downloadProgress = observation.downloadProgress,
                        isFavorite = gameId in favoriteIds,
                    ),
                    gameCatalog = observation.catalog,
                )
            }

            else -> {
                GamePreviewLogger.w("OBS") { "game emitted NotFound for gameId=$gameId" }
                if (initialLoadStarted) {
                    logger.warning(
                        message = "Preview story not found locally for gameId=$gameId",
                        data = mapOf("gameId" to gameId)
                    )
                }
                GamePreviewUiState.NotFound
            }
        }
    }.catch { error ->
        GamePreviewLogger.e("OBS", error) { "game observation failed for gameId=$gameId" }
        logger.exception(error) { "Failed to observe game state for gameId=$gameId" }
        emit(GamePreviewUiState.Error(GameUiError.Load))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = GamePreviewUiState.Loading,
    )

    val isPurchasing: StateFlow<Boolean> = purchaseHandler.isPurchasing
    val isPurchaseLoading: StateFlow<Boolean> = purchaseHandler.isPurchaseLoading

    val isUserPremium: StateFlow<Boolean> = gamePurchaseRepository.observeHasGlobalPremium()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    private val currentGameItem: GameItem?
        get() = (game.value as? GamePreviewUiState.Data)?.item

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var coinGrantCheckDone = false
    private var coinGrantCheckJob: Job? = null
    private var initialLoadStarted = false
    private var recoveryAttempted = false

    private val _events = MutableSharedFlow<GamePreviewEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Triggers the initial data load. Must be called by the UI once the screen
     * is attached. [loadChapters] is idempotent, so calling this again after a
     * configuration change is safe.
     */
    fun start() {
        GamePreviewLogger.i("LIFE") { "start() called for gameId=$gameId" }
        initialLoadStarted = true
        viewModelScope.launch {
            loadChapters()
        }
        viewModelScope.launch {
            recoverMissingCatalogOnNotFound()
        }
        viewModelScope.launch {
            refreshCatalogOnDataLoad()
        }
        viewModelScope.launch {
            syncCoinPurchaseGrantOnDataLoad()
        }
    }

    /**
     * Waits for the first Data state, then refreshes the catalog row remotely so
     * the observed catalog (including the admin version badge) converges to the
     * server state. A missing catalog is the recovery path's job, not ours.
     */
    private suspend fun refreshCatalogOnDataLoad() {
        game.first { it is GamePreviewUiState.Data }
        refreshCatalogFromRemote()
    }

    /**
     * Best-effort remote refresh of this story's catalog row. Failures are
     * non-fatal: the cached row keeps feeding the UI.
     */
    private suspend fun refreshCatalogFromRemote() {
        gameRepository.refreshGameCatalog(gameId, Locale.getDefault().toLanguageTag())
            .onSuccess { catalog ->
                GamePreviewLogger.i("SYNC") { "catalog refresh ${if (catalog != null) "updated" else "found no story"} for gameId=$gameId" }
            }
            .onFailure { error ->
                GamePreviewLogger.w("SYNC") { "catalog refresh failed for gameId=$gameId: ${error.message}" }
            }
    }

    /**
     * One-shot remote recovery: waits for the first NotFound, then fetches the
     * catalog remotely once. On success the Room upsert makes [game] re-emit
     * Data reactively; no manual state mutation here.
     */
    private suspend fun recoverMissingCatalogOnNotFound() {
        game.first { it is GamePreviewUiState.NotFound }
        attemptCatalogRecovery()
    }

    private suspend fun attemptCatalogRecovery() {
        if (recoveryAttempted) {
            GamePreviewLogger.d("SYNC") { "catalog recovery already attempted for gameId=$gameId" }
            return
        }
        recoveryAttempted = true
        GamePreviewLogger.i("SYNC") { "catalog recovery started for gameId=$gameId" }
        gameRepository.getGameCatalog(gameId, Locale.getDefault().toLanguageTag())
            .onSuccess { catalog ->
                GamePreviewLogger.i("SYNC") { "catalog recovery ${if (catalog != null) "succeeded" else "found no story"} for gameId=$gameId" }
            }
            .onFailure { error ->
                GamePreviewLogger.e("SYNC", error) { "catalog recovery failed for gameId=$gameId" }
                logger.warning(
                    message = "Preview story remote recovery failed for gameId=$gameId",
                    data = mapOf("gameId" to gameId)
                )
            }
    }

    fun onAction(action: GamePreviewAction) {
        when (action) {
            GamePreviewAction.OnBuy -> onBuy()
            GamePreviewAction.OnAbortBuy -> purchaseHandler.abortPurchaseFlow()
            GamePreviewAction.OnBuyConfirm -> onPurchase()
            GamePreviewAction.OnDownload -> onStartDownload()
            GamePreviewAction.OnUpdateGame -> onStartDownload()
            GamePreviewAction.OnUpdateApp -> sendEvent(GamePreviewEvent.OpenAppStore)
            GamePreviewAction.OnPlay -> onPlay()
            GamePreviewAction.OnTry -> navigateToPlay(requestNickName = true, isTrial = true)
            GamePreviewAction.OnRestart -> sendEvent(GamePreviewEvent.ShowRestartDialog)
            GamePreviewAction.OnRestartConfirm -> onRestartGame()
            GamePreviewAction.OnDelete -> onDeleteGame()
            GamePreviewAction.OnToggleFavorite -> onToggleFavorite()
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
        if (_isRefreshing.value) {
            GamePreviewLogger.d("LIFE") { "refresh() ignored: already refreshing for gameId=$gameId" }
            return
        }
        GamePreviewLogger.i("LIFE") { "refresh() started for gameId=$gameId" }
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Explicit user refresh grants one extra recovery attempt.
                if (game.value is GamePreviewUiState.NotFound) {
                    recoveryAttempted = false
                    attemptCatalogRecovery()
                }
                // Explicit user refresh also grants a fresh coin grant check round.
                coinGrantCheckDone = false
                triggerCoinGrantCheck()
                if (game.value is GamePreviewUiState.Data) {
                    refreshCatalogFromRemote()
                }
                val chaptersOk = loadChapters()
                if (!chaptersOk) {
                    GamePreviewLogger.w("SYNC") { "refresh() failed for gameId=$gameId" }
                    logger.warning(
                        message = "Preview refresh failed for gameId=$gameId",
                        data = mapOf("gameId" to gameId)
                    )
                } else {
                    GamePreviewLogger.i("LIFE") { "refresh() completed for gameId=$gameId" }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onNickNameConfirmed(name: String?, isTrial: Boolean) {
        GamePreviewLogger.d("NAV") {
            "onNickNameConfirmed() gameId=$gameId, isTrial=$isTrial, name=${
                name?.take(
                    20
                )
            }"
        }
        viewModelScope.launch {
            saveUserNickNameUseCase(gameId, name)
            navigateToPlay(requestNickName = false, isTrial = isTrial)
        }
    }

    private fun onBuy() {
        if (!isUserConnected.value) {
            GamePreviewLogger.d("PUR") { "onBuy() user not connected for gameId=$gameId" }
            sendEvent(GamePreviewEvent.OpenAccountConnection)
            return
        }
        GamePreviewLogger.i("PUR") { "onBuy() starting purchase flow for gameId=$gameId" }
        purchaseHandler.startPurchaseFlow()
    }

    /**
     * Single gatekeeper for starting the game. An unreleased chapter no longer
     * disables the Play button: tapping it surfaces the same message as
     * GamePreviewUnavailable as a toast instead. Administrators bypass the check.
     */
    private fun onPlay() {
        val chapter = currentChapter.value
        when {
            // Chapter still loading: the Play button is disabled in this state.
            chapter == null -> Unit
            !chapter.available && !isAdmin.value -> {
                GamePreviewLogger.d("NAV") { "onPlay() chapter ${chapter.number} unavailable for gameId=$gameId" }
                toastService(
                    R.string.game_presentation_game_preview_next_chapter,
                    chapter.formatReleaseDate(),
                )
            }

            else -> navigateToPlay(requestNickName = true)
        }
    }

    private fun navigateToPlay(requestNickName: Boolean, isTrial: Boolean = false) {
        val data = game.value as? GamePreviewUiState.Data ?: run {
            GamePreviewLogger.w("NAV") { "navigateToPlay() ignored: no data for gameId=$gameId" }
            return
        }
        viewModelScope.launch {
            val chapter = currentChapter.value
            val needsNickName = data.gameCatalog.userNickNameRequired &&
                    chapter?.number == 1 && requestNickName

            if (chapter == null) {
                GamePreviewLogger.w("NAV") { "navigateToPlay() called with null currentChapter for gameId=$gameId" }
                logger.warning(
                    message = "Preview navigateToPlay() called with null currentChapter for gameId=$gameId",
                    data = mapOf("gameId" to gameId)
                )
            }

            GamePreviewLogger.i("NAV") {
                "navigateToPlay() gameId=$gameId, isTrial=$isTrial, " +
                        "chapterCode=${chapter?.normalizedCode}, needsNickName=$needsNickName"
            }

            if (needsNickName) {
                sendEvent(GamePreviewEvent.RequestNickName(isTrial = isTrial))
            } else {
                sendEvent(
                    GamePreviewEvent.PlayGame(
                        gameId = gameId,
                        legacyId = data.gameCatalog.legacyId,
                        isPurchased = data.item.isPurchased,
                        chapterCode = chapter?.normalizedCode,
                        isTrial = isTrial,
                    )
                )
            }
        }
    }

    private fun sendEvent(event: GamePreviewEvent) {
        GamePreviewLogger.d("LIFE") { "sendEvent() ${event::class.simpleName} for gameId=$gameId" }
        if (event is GamePreviewEvent.ShowError) {
            toastService(event.error.stringRes)
        }
        _events.tryEmit(event)
    }

    override fun onCleared() {
        GamePreviewLogger.i("LIFE") { "GamePreviewViewModel cleared for gameId=$gameId" }
        super.onCleared()
    }

    /** @return false when the chapters load reports a failure. */
    private suspend fun loadChapters(): Boolean {
        GamePreviewLogger.d("CHAP") { "loadChapters() started for gameId=$gameId" }
        var success = true
        getChaptersUseCase(gameId)
            .collect { result ->
                result.onSuccess { chapters ->
                    GamePreviewLogger.i("CHAP") {
                        "loadChapters() received ${chapters.size} chapter(s) for gameId=$gameId"
                    }
                    if (chapters.isEmpty()) {
                        GamePreviewLogger.w("CHAP") { "loadChapters() returned empty chapter list for gameId=$gameId" }
                        logger.warning(
                            message = "Preview loaded empty chapter list for gameId=$gameId",
                            data = mapOf("gameId" to gameId)
                        )
                    }
                }
                result.onFailure { error ->
                    success = false
                    GamePreviewLogger.e(
                        "CHAP",
                        error
                    ) { "loadChapters() failed for gameId=$gameId" }
                    logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
                }
            }
        GamePreviewLogger.d("CHAP") { "loadChapters() finished with success=$success for gameId=$gameId" }
        return success
    }

    /**
     * Reactive healing: coin purchase grants live in memory only, so whenever
     * the screen shows an unbought paid story while the user is connected, we
     * ask the server. Combining with [isUserConnected] re-triggers the check
     * when the user connects after the data has loaded.
     */
    private suspend fun syncCoinPurchaseGrantOnDataLoad() {
        GamePreviewLogger.d("PUR") { "syncCoinPurchaseGrantOnDataLoad() started for gameId=$gameId" }
        combine(game, isUserConnected, ::Pair).collect {
            triggerCoinGrantCheck()
        }
    }

    /**
     * Single entry point for the coin grant check. Guards against re-entrance:
     * at most one check job in flight, and a definitive server answer
     * ([coinGrantCheckDone]) stops further checks until [refresh].
     */
    private fun triggerCoinGrantCheck() {
        val data = game.value as? GamePreviewUiState.Data ?: return
        if (coinGrantCheckDone || coinGrantCheckJob?.isActive == true ||
            !isUserConnected.value || data.item.isPurchased || data.gameCatalog.skus.isEmpty()
        ) {
            GamePreviewLogger.d("PUR") {
                "coin grant check skipped for gameId=$gameId: " +
                        "done=$coinGrantCheckDone, inFlight=${coinGrantCheckJob?.isActive == true}, " +
                        "connected=${isUserConnected.value}, " +
                        "isPurchased=${data.item.isPurchased}, hasSkus=${data.gameCatalog.skus.isNotEmpty()}"
            }
            return
        }
        coinGrantCheckJob = viewModelScope.launch { attemptCoinGrantCheck(data.gameCatalog.skus) }
    }

    /**
     * Bounded retry: transient failures (network, 5xx, user-not-loaded-yet)
     * get up to [MAX_GRANT_CHECK_ATTEMPTS] attempts with linear backoff. Only
     * a definitive server answer marks [coinGrantCheckDone]; after exhausting
     * the attempts we give up silently — pull-to-refresh grants a fresh round.
     */
    private suspend fun attemptCoinGrantCheck(skus: List<String>) {
        var attempt = 0
        while (attempt < MAX_GRANT_CHECK_ATTEMPTS && !coinGrantCheckDone) {
            attempt++
            GamePreviewLogger.i("PUR") { "coin grant check attempt $attempt/$MAX_GRANT_CHECK_ATTEMPTS for gameId=$gameId" }
            isStoryGrantedUseCase(skus)
                .onSuccess { granted ->
                    coinGrantCheckDone = true
                    GamePreviewLogger.i("PUR") { "coin grant check answered granted=$granted for gameId=$gameId" }
                }
                .onFailure { error ->
                    GamePreviewLogger.e(
                        "PUR",
                        error
                    ) { "coin grant check attempt $attempt failed for gameId=$gameId" }
                    if (attempt == MAX_GRANT_CHECK_ATTEMPTS) {
                        logger.warning(
                            message = "Coin purchase grant check gave up after $MAX_GRANT_CHECK_ATTEMPTS attempts for gameId=$gameId",
                            data = mapOf("gameId" to gameId),
                        )
                    }
                }
            if (!coinGrantCheckDone && attempt < MAX_GRANT_CHECK_ATTEMPTS) {
                delay(GRANT_CHECK_RETRY_DELAY_MS * attempt)
            }
        }
    }

    private fun onStartDownload() {
        GamePreviewLogger.i("DOWN") { "onStartDownload() gameId=$gameId" }
        viewModelScope.launch {
            // The use case is suspend and can throw before returning its flow (game not
            // cached, download link fetch failed offline): .catch only covers collection.
            try {
                downloadGameUseCase(gameId = gameId)
                    .catch { error ->
                        GamePreviewLogger.e(
                            "DOWN",
                            error
                        ) { "onStartDownload() failed for gameId=$gameId" }
                        logger.exception(error) { "Download failed for gameId=$gameId" }
                        sendEvent(GamePreviewEvent.ShowError(GameUiError.Download))
                    }
                    .collect { progress ->
                        GamePreviewLogger.d("DOWN") { "onStartDownload() progress=$progress for gameId=$gameId" }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                GamePreviewLogger.e("DOWN", e) { "onStartDownload() failed for gameId=$gameId" }
                logger.exception(e) { "Download failed for gameId=$gameId" }
                sendEvent(GamePreviewEvent.ShowError(GameUiError.Download))
            }
        }
    }

    private fun onDeleteGame() {
        GamePreviewLogger.i("DOWN") { "onDeleteGame() gameId=$gameId" }
        viewModelScope.launch {
            gameInstallRepository.deleteGame(gameId)
                .onSuccess {
                    GamePreviewLogger.i("DOWN") { "onDeleteGame() succeeded for gameId=$gameId" }
                }
                .onFailure { error ->
                    GamePreviewLogger.e(
                        "DOWN",
                        error
                    ) { "onDeleteGame() failed for gameId=$gameId" }
                    logger.exception(error) { "Delete failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Delete))
                }
        }
    }

    private fun onPurchase() {
        val sku = currentGameItem?.skuIdentifiers?.firstOrNull()
        if (sku == null) {
            GamePreviewLogger.w("PUR") { "onPurchase() no SKU for gameId=$gameId" }
            logger.warning("No SKU available for purchase for gameId=$gameId")
            purchaseHandler.abortPurchaseFlow()
            sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
            return
        }

        GamePreviewLogger.i("PUR") { "onPurchase() confirming sku=$sku for gameId=$gameId" }
        viewModelScope.launch {
            purchaseHandler.confirmPurchase(sku)
                .onSuccess {
                    GamePreviewLogger.i("PUR") { "onPurchase() succeeded for sku=$sku" }
                    sendEvent(GamePreviewEvent.PurchaseSuccess)
                }
                .onFailure { error ->
                    GamePreviewLogger.e("PUR", error) { "onPurchase() failed for sku=$sku" }
                    logger.exception(error) { "Purchase failed for sku=$sku" }
                    when (error) {
                        is BuyStoryError.AlreadyOwned -> sendEvent(GamePreviewEvent.ShowAlreadyBoughtAlert)
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
                    // Progress was just wiped: re-read the current chapter so the
                    // Play button shows chapter 1 again (Friendzoned progress lives
                    // outside Room and would otherwise stay stale until ON_RESUME).
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
        const val OPTIONS_ACCESS_UID = "8be954c7a18f4e7cba9c"
        const val MAX_GRANT_CHECK_ATTEMPTS = 3
        const val GRANT_CHECK_RETRY_DELAY_MS = 1_000L
    }
}
