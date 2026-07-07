package com.purpletear.game.presentation.game_preview

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    gameRepository: GameRepository,
    chapterRepository: ChapterRepository,
    gamePurchaseRepository: PurchaseRepository,
    gameInstallRepository: GameInstallRepository,
    mediaUrlResolver: MediaUrlResolver,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val saveUserNickNameUseCase: SaveUserNickNameUseCase,
    private val makeToastService: MakeToastService,
    restartGameUseCase: RestartGameUseCase,
    downloadGameUseCase: DownloadGameUseCase,
    private val logger: Logger,
    appVersionProvider: AppVersionProvider,
) : BaseGameViewModel(
    savedStateHandle = savedStateHandle,
    gameRepository = gameRepository,
    chapterRepository = chapterRepository,
    gameInstallRepository = gameInstallRepository,
    gamePurchaseRepository = gamePurchaseRepository,
    mediaUrlResolver = mediaUrlResolver,
    restartGameUseCase = restartGameUseCase,
    downloadGameUseCase = downloadGameUseCase,
) {

    val appBuildNumber: Int = appVersionProvider.getVersionCode()

    private val _events = Channel<GamePreviewEvent>(Channel.CONFLATED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            loadChapters()
        }
    }

    fun onAction(action: GamePreviewAction) {
        when (action) {
            GamePreviewAction.OnBuy -> _isPurchasing.value = true
            GamePreviewAction.OnAbortBuy -> resetPurchaseState()
            GamePreviewAction.OnBuyConfirm -> onPurchase()
            GamePreviewAction.OnDownload -> onStartDownload()
            GamePreviewAction.OnUpdateGame -> onStartDownload()
            GamePreviewAction.OnUpdateApp -> sendEvent(GamePreviewEvent.OpenAppStore)
            GamePreviewAction.OnPlay -> navigateToPlay(true)
            GamePreviewAction.OnRestart -> sendEvent(GamePreviewEvent.ShowRestartDialog)
            GamePreviewAction.OnRestartConfirm -> onRestartGame()
            GamePreviewAction.OnDelete -> onDeleteGame()
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

    fun onNickNameConfirmed(name: String?) {
        viewModelScope.launch {
            saveUserNickNameUseCase(gameId, name)
            navigateToPlay(false)
        }
    }

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

    private fun onStartDownload() {
        viewModelScope.launch {
            super.startDownload()
                .catch { error ->
                    logger.exception(error) { "Download failed for gameId=$gameId" }
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Download))
                }
                .collect { progress ->
                    Log.d(TAG, "Download progress for gameId=$gameId: $progress")
                }
        }
    }

    private fun onDeleteGame() {
        viewModelScope.launch {
            super.deleteGame()
                .onFailure { error ->
                    Log.e(TAG, "Delete failed for gameId=$gameId", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Delete))
                }
        }
    }

    private fun onPurchase() {
        val sku = currentGameItem?.skuIdentifiers?.firstOrNull()
        if (sku == null) {
            Log.w(TAG, "No SKU available for purchase for gameId=$gameId")
            resetPurchaseState()
            sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
            return
        }

        viewModelScope.launch {
            purchaseWithState(sku)
                .onSuccess { sendEvent(GamePreviewEvent.PurchaseSuccess) }
                .onFailure { error ->
                    Log.e(TAG, "Purchase failed for sku=$sku", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Purchase))
                }
        }
    }

    private fun onRestartGame() {
        viewModelScope.launch {
            super.restartGame()
                .onSuccess {
                    makeToastService(R.string.game_restart_success)
                }
                .onFailure { error ->
                    Log.e(TAG, "Restart failed for gameId=$gameId", error)
                    sendEvent(GamePreviewEvent.ShowError(GameUiError.Restart))
                }
        }
    }

    companion object {
        private const val TAG = "GamePreviewViewModel"
    }
}
