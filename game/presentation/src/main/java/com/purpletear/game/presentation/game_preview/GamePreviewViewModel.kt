package com.purpletear.game.presentation.game_preview

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.game.presentation.common.states.GameButtonsState
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GamePreviewUiState {
    data object Loading : GamePreviewUiState

    @Keep
    data class Data(val item: GameItem, val gameCatalog: GameCatalog) : GamePreviewUiState
    data object NotFound : GamePreviewUiState

    @Keep
    data class Error(val error: GameUiError) : GamePreviewUiState
}

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    gameRepository: GameRepository,
    chapterRepository: ChapterRepository,
    gamePurchaseRepository: PurchaseRepository,
    gameInstallRepository: GameInstallRepository,
    mediaUrlResolver: MediaUrlResolver,
) : ViewModel() {

    private val _events = Channel<GamePreviewEvent>(Channel.CONFLATED)
    val events = _events.receiveAsFlow()

    val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

    val isUserPremium: StateFlow<Boolean> = gamePurchaseRepository.observeHasGlobalPremium()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    val currentChapter: StateFlow<Chapter?> = chapterRepository.observeCurrentChapter(gameId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = null,
        )

    internal val gameButtonsState: GameButtonsState
        get() = GameButtonsState() // TODO: derive from game state and current chapter

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
        // If gameId comes from SavedStateHandle (full-screen mode), auto-initialize
        gameId.let { id ->
            viewModelScope.launch {

            }
        }
    }
}
