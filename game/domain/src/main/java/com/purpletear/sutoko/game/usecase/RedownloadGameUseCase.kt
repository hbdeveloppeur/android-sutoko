package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for redownloading a game.
 * Deletes the existing game files and metadata, then re-downloads from scratch.
 */
class RedownloadGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val downloadGameUseCase: DownloadGameUseCase,
) {
    /**
     * Redownloads the game files.
     *
     * Flow:
     * 1. Remove existing game installation (files + metadata)
     * 2. Fetch game details from API
     * 3. Download the game using DownloadGameUseCase
     *
     * @param gameId The ID of the game to redownload.
     * @param userId The user ID for authenticated downloads (required for premium games).
     * @param userToken The user token for authenticated downloads (required for premium games).
     * @param isUserConnected Whether the user is currently authenticated.
     * @return Result containing Unit on success, or exception on failure.
     */
    suspend operator fun invoke(
        gameId: String,
        userId: String?,
        userToken: String?,
        isUserConnected: Boolean,
    ): Result<Unit> {
        // Step 1: Remove existing game installation (files + metadata)
        // GameRepository.removeGame delegates to GameInstallationRepository.removeInstallation
        // which handles both DB deletion and file cleanup atomically
        gameRepository.removeGame(gameId).first().getOrElse {
            return Result.failure(Exception("Failed to remove existing game: ${it.message}", it))
        }

        // Step 2: Fetch game details from API
        val gameResult = gameRepository.getGame(gameId).first()
        val game = gameResult.getOrElse {
            return Result.failure(Exception("Failed to fetch game details: ${it.message}", it))
        }

        // Step 3: Download the game using DownloadGameUseCase
        // This handles authentication validation for premium games
        return downloadGameUseCase(
            game = game,
            userId = userId,
            userToken = userToken,
            isUserConnected = isUserConnected,
        )
    }
}
