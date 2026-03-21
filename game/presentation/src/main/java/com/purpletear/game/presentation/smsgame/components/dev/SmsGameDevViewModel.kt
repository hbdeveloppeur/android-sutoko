package com.purpletear.game.presentation.smsgame.components.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.usecase.ObserveDownloadStateUseCase
import com.purpletear.sutoko.game.usecase.RedownloadGameUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for debug/dev components in SMS game.
 * Handles developer commands and tools.
 */
@HiltViewModel
class SmsGameDevViewModel @Inject constructor(
    private val restartGame: RestartGameUseCase,
    private val redownloadGame: RedownloadGameUseCase,
    private val observeDownloadStateUseCase: ObserveDownloadStateUseCase,
    private val makeToastService: MakeToastService,
    private val customer: Customer,
) : ViewModel() {

    /**
     * Restarts the game by deleting all user progress.
     * Shows a toast on success or failure.
     */
    fun restart(gameId: String) {
        viewModelScope.launch {
            restartGame(gameId)
                .onSuccess { toast(R.string.game_restart_success) }
                .onFailure { toast(R.string.game_restart_error) }
        }
    }

    /**
     * Redownloads the game files.
     * Deletes existing files and metadata, then re-downloads from scratch.
     * Shows toasts for: start, completion, and error states.
     */
    fun redownload(gameId: String) {
        viewModelScope.launch {
            observeDownloadProgress(gameId)
            startRedownload(gameId)
        }
    }

    /**
     * Observes download progress and shows toast on completion or error.
     * Runs in parallel with the download operation.
     */
    private fun observeDownloadProgress(gameId: String) = viewModelScope.launch {
        observeDownloadStateUseCase(gameId)
            .takeWhile { !it.isTerminal() }
            .collect { state ->
                when (state) {
                    is GameDownloadState.Completed -> toast(R.string.game_redownload_completed)
                    is GameDownloadState.Error -> toast(R.string.game_redownload_error)
                    else -> { /* Downloading, Extracting, Idle, Cancelled - no toast */
                    }
                }
            }
    }

    /**
     * Triggers the redownload use case with appropriate user credentials.
     */
    private suspend fun startRedownload(gameId: String) {
        val credentials = getUserCredentials()

        redownloadGame(
            gameId = gameId,
            userId = credentials.userId,
            userToken = credentials.userToken,
            isUserConnected = credentials.isConnected,
        )
            .onSuccess { toast(R.string.game_redownload_started) }
            .onFailure { toast(R.string.game_redownload_error) }
    }

    /**
     * Returns user credentials if connected, nulls otherwise.
     * Premium games require authentication; free games don't.
     */
    private fun getUserCredentials(): UserCredentials {
        val isConnected = customer.isUserConnected()
        return UserCredentials(
            userId = if (isConnected) customer.getUserId() else null,
            userToken = if (isConnected) customer.getUserToken() else null,
            isConnected = isConnected,
        )
    }

    private fun toast(messageResId: Int) {
        makeToastService(messageResId)
    }

    private fun GameDownloadState.isTerminal(): Boolean {
        return this is GameDownloadState.Completed || this is GameDownloadState.Error
    }
}

private data class UserCredentials(
    val userId: String?,
    val userToken: String?,
    val isConnected: Boolean,
)
