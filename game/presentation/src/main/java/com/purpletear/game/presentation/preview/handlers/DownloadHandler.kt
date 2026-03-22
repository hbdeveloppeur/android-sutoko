package com.purpletear.game.presentation.preview.handlers

import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.ObserveDownloadStateUseCase
import com.purpletear.sutoko.user.usecase.OpenSignInPageUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Handles game download and update operations.
 * Encapsulates download initiation, state observation, and error handling.
 */
class DownloadHandler @Inject constructor(
    private val downloadGameUseCase: DownloadGameUseCase,
    private val observeDownloadStateUseCase: ObserveDownloadStateUseCase,
    private val gameDownloadManager: GameDownloadManager,
    private val customer: Customer,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
) {

    /**
     * Result of a download attempt.
     */
    sealed class Result {
        data object Success : Result()
        data class Error(val exception: Throwable) : Result()
    }

    /**
     * Initiates download for a game.
     * Redirects to sign-in if user is not connected for premium games.
     *
     * @param game The game to download
     * @return Result indicating success or error
     */
    suspend fun startDownload(game: Game): Result {
        if (game.isPremium() && !customer.isUserConnected()) {
            openSignInPageUseCase()
            return Result.Error(SecurityException("User not connected for premium download"))
        }

        val isPremium = game.isPremium()
        return downloadGameUseCase(
            game = game,
            userId = if (isPremium) customer.getUserId() else null,
            userToken = if (isPremium) customer.getUserToken() else null,
            isUserConnected = customer.isUserConnected()
        ).fold(
            onSuccess = { Result.Success },
            onFailure = { error ->
                when (error) {
                    is GameDownloadForbiddenException -> {
                        android.util.Log.e("DownloadHandler", "Forbidden access: ${error.message}")
                    }
                    else -> {
                        android.util.Log.e("DownloadHandler", "Download failed: ${error.message}")
                    }
                }
                Result.Error(error)
            }
        )
    }

    /**
     * Observes download state changes for a game.
     *
     * @param gameId The ID of the game to observe
     * @return Flow of download states
     */
    fun observeDownloadState(gameId: String): Flow<GameDownloadState> {
        return observeDownloadStateUseCase(gameId)
    }

    /**
     * Cleans up resources when the handler is no longer needed.
     *
     * @param gameId The ID of the game to cleanup
     */
    fun cleanup(gameId: String) {
        gameDownloadManager.cleanup(gameId)
    }
}
