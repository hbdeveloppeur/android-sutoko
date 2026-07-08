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
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val game: StateFlow<GamePreviewUiState> = combine(
        gameRepository.observeGame(id = gameId),
        gameInstallRepository.observeInstall(gameId = gameId),
        gamePurchaseRepository.observePurchasedSkus(),
        gameInstallRepository.observeDownloadProgress(gameId)
    ) { catalog, install, purchasedSkus, downloadProgress ->
        when {
            catalog != null -> GamePreviewUiState.Data(
                item = GameItem(
                    catalog,
                    install,
                    isPurchased = catalog.skus.any { it in purchasedSkus },
                    bannerUrl = mediaUrlResolver.resolveBannerUrl(catalog.banner?.storagePath),
                    logoUrl = mediaUrlResolver.resolveBannerUrl(catalog.logo?.storagePath),
                    menuBackgroundUrl = mediaUrlResolver.resolveBannerUrl(catalog.menuBackground?.storagePath),
                    downloadProgress,
                ),
                gameCatalog = catalog,
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
    }

    fun onAction(action: GamePreviewAction) {
        when (action) {
            GamePreviewAction.OnBuy -> purchaseHandler.startPurchaseFlow()
            GamePreviewAction.OnAbortBuy -> purchaseHandler.abortPurchaseFlow()
            GamePreviewAction.OnBuyConfirm -> onPurchase()
            GamePreviewAction.OnDownload -> onStartDownload()
            GamePreviewAction.OnUpdateGame -> onStartDownload()
            GamePreviewAction.OnUpdateApp -> sendEvent(GamePreviewEvent.OpenAppStore)
            GamePreviewAction.OnPlay -> navigateToPlay(requestNickName = true)
            GamePreviewAction.OnRestart -> sendEvent(GamePreviewEvent.ShowRestartDialog)
            GamePreviewAction.OnRestartConfirm -> onRestartGame()
            GamePreviewAction.OnDelete -> onDeleteGame()
        }
    }

    fun onNickNameConfirmed(name: String?) {
        viewModelScope.launch {
            saveUserNickNameUseCase(gameId, name)
            navigateToPlay(requestNickName = false)
        }
    }

    private fun navigateToPlay(requestNickName: Boolean) {
        val data = game.value as? GamePreviewUiState.Data ?: return
        viewModelScope.launch {
            val needsNickName = data.gameCatalog.userNickNameRequired &&
                    currentChapter.value?.number == 1 && requestNickName

            if (needsNickName) {
                sendEvent(GamePreviewEvent.RequestNickName)
            } else {
                sendEvent(
                    GamePreviewEvent.PlayGame(
                        gameId = gameId,
                        legacyId = data.gameCatalog.legacyId,
                        isPurchased = data.item.isPurchased,
                    )
                )
            }
        }
    }

    private fun sendEvent(event: GamePreviewEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadChapters() {
        getChaptersUseCase(gameId)
            .collect { result ->
                result.onFailure { error ->
                    logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
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
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
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
