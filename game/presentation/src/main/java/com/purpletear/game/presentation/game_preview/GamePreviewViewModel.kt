package com.purpletear.game.presentation.game_preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.ToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.handlers.GamePreviewPurchaseHandler
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.core.domain.logger.warning
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
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

    val currentChapter: StateFlow<Chapter?> = chapterRepository.observeCurrentChapter(gameId)
        .onEach { chapter ->
            GamePreviewLogger.d("OBS") {
                chapter?.let {
                    "currentChapter emitted: gameId=$gameId, code=${it.code}, number=${it.number}, available=${it.isAvailable}"
                } ?: "currentChapter emitted: null for gameId=$gameId"
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
    ) { observation, coinPurchasedSkus ->
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
            syncCoinPurchaseGrantOnDataLoad()
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
            GamePreviewAction.OnPlay -> navigateToPlay(requestNickName = true)
            GamePreviewAction.OnTry -> navigateToPlay(requestNickName = true, isTrial = true)
            GamePreviewAction.OnRestart -> sendEvent(GamePreviewEvent.ShowRestartDialog)
            GamePreviewAction.OnRestartConfirm -> onRestartGame()
            GamePreviewAction.OnDelete -> onDeleteGame()
        }
    }

    /**
     * Re-fetches this story's chapters from the network. The Room observation
     * flows update the UI automatically when fresh data lands. The catalog row
     * itself is maintained by the app-foreground catalog syncs; an installed
     * game is never evicted by those syncs (see GameDao).
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
        GamePreviewLogger.d("NAV") { "onNickNameConfirmed() gameId=$gameId, isTrial=$isTrial, name=${name?.take(20)}" }
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
                    GamePreviewLogger.e("CHAP", error) { "loadChapters() failed for gameId=$gameId" }
                    logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
                }
            }
        GamePreviewLogger.d("CHAP") { "loadChapters() finished with success=$success for gameId=$gameId" }
        return success
    }

    private suspend fun syncCoinPurchaseGrantOnDataLoad() {
        GamePreviewLogger.d("PUR") { "syncCoinPurchaseGrantOnDataLoad() started for gameId=$gameId" }
        game.collect { state ->
            val data = state as? GamePreviewUiState.Data ?: return@collect
            if (coinGrantCheckDone || !isUserConnected.value || data.item.isPurchased || data.gameCatalog.skus.isEmpty()) {
                GamePreviewLogger.d("PUR") {
                    "syncCoinPurchaseGrantOnDataLoad() skipped for gameId=$gameId: " +
                        "alreadyChecked=$coinGrantCheckDone, connected=${isUserConnected.value}, " +
                        "isPurchased=${data.item.isPurchased}, hasSkus=${data.gameCatalog.skus.isNotEmpty()}"
                }
                return@collect
            }

            coinGrantCheckDone = true
            GamePreviewLogger.i("PUR") { "syncCoinPurchaseGrantOnDataLoad() checking SKUs for gameId=$gameId" }
            isStoryGrantedUseCase(data.gameCatalog.skus)
                .onSuccess {
                    GamePreviewLogger.i("PUR") { "syncCoinPurchaseGrantOnDataLoad() completed for gameId=$gameId" }
                }
                .onFailure { error ->
                    GamePreviewLogger.e("PUR", error) { "syncCoinPurchaseGrantOnDataLoad() failed for gameId=$gameId" }
                    logger.exception(error) { "Failed to sync coin purchase grant for gameId=$gameId" }
                }
        }
    }

    private fun onStartDownload() {
        GamePreviewLogger.i("DOWN") { "onStartDownload() gameId=$gameId" }
        viewModelScope.launch {
            downloadGameUseCase(gameId = gameId)
                .catch { error ->
                    GamePreviewLogger.e("DOWN", error) { "onStartDownload() failed for gameId=$gameId" }
                    logger.exception(error) { "Download failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Download))
                }
                .collect { progress ->
                    GamePreviewLogger.d("DOWN") { "onStartDownload() progress=$progress for gameId=$gameId" }
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
                    GamePreviewLogger.e("DOWN", error) { "onDeleteGame() failed for gameId=$gameId" }
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
            restartGameUseCase(gameId)
                .onSuccess {
                    GamePreviewLogger.i("LIFE") { "onRestartGame() succeeded for gameId=$gameId" }
                    toastService(R.string.game_presentation_game_restart_success)
                }
                .onFailure { error ->
                    GamePreviewLogger.e("LIFE", error) { "onRestartGame() failed for gameId=$gameId" }
                    logger.exception(error) { "Restart failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Restart))
                }
        }
    }
}
