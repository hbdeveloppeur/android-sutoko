package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.exception.UserNotConnectedException
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import javax.inject.Inject

/**
 * Use case for downloading a game.
 * Encapsulates business rules: authentication validation for premium games
 * and download initiation.
 */
class DownloadGameUseCase @Inject constructor(
    private val gameDownloadManager: GameDownloadManager,
) {
    /**
     * Invoke the use case to download a game.
     *
     * Business rules applied:
     * - Premium games require user authentication (userId and userToken)
     * - Free games can be downloaded without authentication
     *
     * @param game The game to download.
     * @param userId The user ID for authenticated downloads (required for premium games).
     * @param userToken The user token for authenticated downloads (required for premium games).
     * @param isUserConnected Whether the user is currently authenticated.
     * @return Result containing Unit on success, or exception on failure.
     * @throws UserNotConnectedException if premium game and user not authenticated.
     */
    suspend operator fun invoke(
        game: Game,
        userId: String?,
        userToken: String?,
        isUserConnected: Boolean,
    ): Result<Unit> {
        // Business rule: Premium games require authenticated user
        if (game.isPremium() && !isUserConnected) {
            return Result.failure(UserNotConnectedException())
        }

        val actualUserId = if (game.isPremium()) userId else null
        val actualUserToken = if (game.isPremium()) userToken else null

        return try {
            gameDownloadManager.downloadGame(
                game = game,
                userId = actualUserId,
                userToken = actualUserToken
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
