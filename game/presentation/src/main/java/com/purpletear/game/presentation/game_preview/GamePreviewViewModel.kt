package com.purpletear.game.presentation.game_preview

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GamePreviewUiState {
    data object Loading : GamePreviewUiState

    @Keep
    data class Data(
        val item: GameItem,
        val gameCatalog: com.purpletear.sutoko.game.model.game.GameCatalog
    ) : GamePreviewUiState

    data object NotFound : GamePreviewUiState

    @Keep
    data class Error(val error: GameUiError) : GamePreviewUiState
}

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    chapterRepository: ChapterRepository,
    private val gamePurchaseRepository: PurchaseRepository,
    private val gameInstallRepository: GameInstallRepository,
    mediaUrlResolver: MediaUrlResolver,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val downloadGameUseCase: DownloadGameUseCase,
    private val restartGameUseCase: RestartGameUseCase,
    private val userRepository: UserRepository,
    private val makeToastService: MakeToastService,
    appVersionProvider: AppVersionProvider,
) : ViewModel() {

    val appBuildNumber: Int = appVersionProvider.getVersionCode()

    private val _events = Channel<GamePreviewEvent>(Channel.CONFLATED)
    val events = _events.receiveAsFlow()

    val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _isPurchaseLoading = MutableStateFlow(false)
    val isPurchaseLoading: StateFlow<Boolean> = _isPurchaseLoading.asStateFlow()

    val isUserPremium: StateFlow<Boolean> = gamePurchaseRepository.observeHasGlobalPremium()

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    val currentChapter: StateFlow<Chapter?> = chapterRepository.observeCurrentChapter(gameId)
        .map {
            it
        }
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
    }.catch { e ->
        emit(GamePreviewUiState.Error(GameUiError.Load))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = GamePreviewUiState.Loading,
    )

    init {
        initializeFromSavedState()
    }

    private fun initializeFromSavedState() {
        viewModelScope.launch {
            loadChapters()
        }
    }

    fun onAction(action: GamePreviewAction) {
        when (action) {
            GamePreviewAction.OnBuy -> _isPurchasing.value = true
            GamePreviewAction.OnAbortBuy -> resetPurchaseState()
            GamePreviewAction.OnBuyConfirm -> startPurchase()
            GamePreviewAction.OnDownload -> startDownload()
            GamePreviewAction.OnUpdateGame -> startDownload()
            GamePreviewAction.OnUpdateApp -> sendEvent(GamePreviewEvent.OpenAppStore)
            GamePreviewAction.OnPlay -> navigateToPlay()
            GamePreviewAction.OnRestart -> sendEvent(GamePreviewEvent.ShowRestartDialog)
            GamePreviewAction.OnRestartConfirm -> restartGame()
            GamePreviewAction.OnDelete -> deleteGame()
        }
    }

    private fun startPurchase() {
        val item = currentItem() ?: return
        val sku = item.skuIdentifiers.firstOrNull()
        if (sku == null) {
            Log.w(TAG, "No SKU available for purchase for gameId=$gameId")
            resetPurchaseState()
            sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
            return
        }

        _isPurchaseLoading.value = true
        viewModelScope.launch {
            gamePurchaseRepository.purchase(sku)
                .onSuccess {
                    resetPurchaseState()
                    sendEvent(GamePreviewEvent.PurchaseSuccess)
                }
                .onFailure { error ->
                    Log.e(TAG, "Purchase failed for sku=$sku", error)
                    resetPurchaseState()
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
                }
        }
    }

    private fun startDownload() {
        viewModelScope.launch {
            val user = userRepository.observeUser().firstOrNull()
            downloadGameUseCase(
                gameId = gameId,
                userId = user?.id,
                userToken = user?.token,
            )
                .catch { error ->
                    Log.e(TAG, "Download failed for gameId=$gameId", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Download))
                }
                .collect { progress ->
                    Log.d(TAG, "Download progress for gameId=$gameId: $progress")
                }
        }
    }

    private fun deleteGame() {
        viewModelScope.launch {
            gameInstallRepository.deleteGame(gameId)
                .onFailure { error ->
                    Log.e(TAG, "Delete failed for gameId=$gameId", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Delete))
                }
        }
    }

    private fun restartGame() {
        viewModelScope.launch {
            restartGameUseCase(gameId)
                .onSuccess {
                    makeToastService(R.string.game_restart_success)
                }
                .onFailure { error ->
                    Log.e(TAG, "Restart failed for gameId=$gameId", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Restart))
                }
        }
    }

    private fun navigateToPlay() {
        val item = currentItem()
        sendEvent(GamePreviewEvent.PlayGame(gameId, item?.isPurchased ?: false))
    }

    private fun resetPurchaseState() {
        _isPurchasing.value = false
        _isPurchaseLoading.value = false
    }

    private fun currentItem(): GameItem? =
        (game.value as? GamePreviewUiState.Data)?.item

    private fun sendEvent(event: GamePreviewEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    private suspend fun loadChapters() {
        getChaptersUseCase(gameId)
            .collect { result ->
                result.onFailure { error ->
                    Log.e(TAG, "Failed to load chapters for gameId=$gameId", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Load))
                }
            }
    }

    companion object {
        private const val TAG = "GamePreviewViewModel"
    }
}
