package com.purpletear.game.presentation.game_preview

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface GamePreviewUiState {
    data object Loading : GamePreviewUiState

    @Keep
    data class Data(
        val item: GameItem,
        val gameCatalog: GameCatalog
    ) : GamePreviewUiState

    data object NotFound : GamePreviewUiState

    @Keep
    data class Error(val error: GameUiError) : GamePreviewUiState
}

abstract class BaseGameViewModel(
    savedStateHandle: SavedStateHandle,
    gameRepository: GameRepository,
    chapterRepository: ChapterRepository,
    private val gameInstallRepository: GameInstallRepository,
    private val gamePurchaseRepository: PurchaseRepository,
    mediaUrlResolver: MediaUrlResolver,
    private val restartGameUseCase: RestartGameUseCase,
    private val downloadGameUseCase: DownloadGameUseCase,
) : ViewModel() {

    protected val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

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
    }.catch { e ->
        emit(GamePreviewUiState.Error(GameUiError.Load))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = GamePreviewUiState.Loading,
    )

    protected val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    protected val _isPurchaseLoading = MutableStateFlow(false)
    val isPurchaseLoading: StateFlow<Boolean> = _isPurchaseLoading.asStateFlow()

    val isUserPremium: StateFlow<Boolean> = gamePurchaseRepository.observeHasGlobalPremium()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    protected val currentGameItem: GameItem?
        get() = (game.value as? GamePreviewUiState.Data)?.item

    protected suspend fun purchaseWithState(sku: String): Result<Unit> {
        _isPurchaseLoading.value = true
        return purchase(sku).also { resetPurchaseState() }
    }

    protected suspend fun purchase(sku: String): Result<Unit> {
        return gamePurchaseRepository.purchase(sku)
    }

    protected fun resetPurchaseState() {
        _isPurchasing.value = false
        _isPurchaseLoading.value = false
    }

    protected suspend fun deleteGame(): Result<Unit> {
        return gameInstallRepository.deleteGame(gameId)
    }

    protected suspend fun restartGame(): Result<Unit> {
        return restartGameUseCase(gameId)
    }

    protected suspend fun startDownload(): Flow<Float> {
        return downloadGameUseCase(gameId = gameId)
    }
}
