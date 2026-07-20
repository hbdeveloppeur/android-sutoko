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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    val appBuildNumber: Int = appVersionProvider.getVersionCode()

    val currentChapter: StateFlow<Chapter?> = chapterRepository.observeCurrentChapter(gameId)
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
            observation.catalog != null -> GamePreviewUiState.Data(
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

            else -> GamePreviewUiState.NotFound
        }
    }.catch { error ->
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

    private val _events = MutableSharedFlow<GamePreviewEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Triggers the initial data load. Must be called by the UI once the screen
     * is attached. [loadChapters] is idempotent, so calling this again after a
     * configuration change is safe.
     */
    fun start() {
        viewModelScope.launch {
            loadChapters()
        }
        viewModelScope.launch {
            syncCoinPurchaseGrantOnDataLoad()
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
     * Re-fetches the catalog and the chapters from the network. The Room
     * observation flows update the UI automatically when fresh data lands.
     */
    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val catalogSynced = async { syncCatalog() }
                val chaptersLoaded = async { loadChapters() }
                val catalogOk = catalogSynced.await()
                val chaptersOk = chaptersLoaded.await()
                if (!catalogOk || !chaptersOk) {
                    toastService(R.string.error_load_game)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onNickNameConfirmed(name: String?, isTrial: Boolean) {
        viewModelScope.launch {
            saveUserNickNameUseCase(gameId, name)
            navigateToPlay(requestNickName = false, isTrial = isTrial)
        }
    }

    private fun onBuy() {
        if (!isUserConnected.value) {
            sendEvent(GamePreviewEvent.OpenAccountConnection)
            return
        }
        purchaseHandler.startPurchaseFlow()
    }

    private fun navigateToPlay(requestNickName: Boolean, isTrial: Boolean = false) {
        val data = game.value as? GamePreviewUiState.Data ?: return
        viewModelScope.launch {
            val needsNickName = data.gameCatalog.userNickNameRequired &&
                    currentChapter.value?.number == 1 && requestNickName

            if (needsNickName) {
                sendEvent(GamePreviewEvent.RequestNickName(isTrial = isTrial))
            } else {
                sendEvent(
                    GamePreviewEvent.PlayGame(
                        gameId = gameId,
                        legacyId = data.gameCatalog.legacyId,
                        isPurchased = data.item.isPurchased,
                        chapterCode = currentChapter.value?.normalizedCode,
                        isTrial = isTrial,
                    )
                )
            }
        }
    }

    private fun sendEvent(event: GamePreviewEvent) {
        _events.tryEmit(event)
    }

    private suspend fun syncCatalog(): Boolean {
        return gameRepository.syncOfficialGames(Locale.getDefault().toLanguageTag())
            .onFailure { error ->
                logger.exception(error) { "Catalog sync failed during refresh for gameId=$gameId" }
            }
            .isSuccess
    }

    /** @return false when the chapters load reports a failure. */
    private suspend fun loadChapters(): Boolean {
        var success = true
        getChaptersUseCase(gameId)
            .collect { result ->
                result.onFailure { error ->
                    success = false
                    logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
                }
            }
        return success
    }

    private suspend fun syncCoinPurchaseGrantOnDataLoad() {
        game.collect { state ->
            val data = state as? GamePreviewUiState.Data ?: return@collect
            if (coinGrantCheckDone || !isUserConnected.value || data.item.isPurchased || data.gameCatalog.skus.isEmpty()) {
                return@collect
            }

            coinGrantCheckDone = true
            isStoryGrantedUseCase(data.gameCatalog.skus)
                .onFailure { error ->
                    logger.exception(error) { "Failed to sync coin purchase grant for gameId=$gameId" }
                }
        }
    }

    private fun onStartDownload() {
        viewModelScope.launch {
            downloadGameUseCase(gameId = gameId)
                .catch { error ->
                    logger.exception(error) { "Download failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Download))
                }
                .collect { /* Progress is observed through gameInstallRepository.observeDownloadProgress */ }
        }
    }

    private fun onDeleteGame() {
        viewModelScope.launch {
            gameInstallRepository.deleteGame(gameId)
                .onFailure { error ->
                    logger.exception(error) { "Delete failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Delete))
                }
        }
    }

    private fun onPurchase() {
        val sku = currentGameItem?.skuIdentifiers?.firstOrNull()
        if (sku == null) {
            logger.warning("No SKU available for purchase for gameId=$gameId")
            purchaseHandler.abortPurchaseFlow()
            sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
            return
        }

        viewModelScope.launch {
            purchaseHandler.confirmPurchase(sku)
                .onSuccess { sendEvent(GamePreviewEvent.PurchaseSuccess) }
                .onFailure { error ->
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
        viewModelScope.launch {
            restartGameUseCase(gameId)
                .onSuccess {
                    toastService(R.string.game_restart_success)
                }
                .onFailure { error ->
                    logger.exception(error) { "Restart failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Restart))
                }
        }
    }
}
