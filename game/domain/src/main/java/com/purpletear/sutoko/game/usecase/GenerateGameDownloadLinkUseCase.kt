package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to generate a download link for a game.
 */
class GenerateGameDownloadLinkUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Generate a download link for a game.
     *
     * @param userId The ID of the user requesting the download
     * @param userToken The token of the user requesting the download
     * @param gameId The ID of the game to download
     * @throws GameDownloadForbiddenException if the game download is forbidden
     * @return A Flow containing the Result of the operation with the download link as a String
     */
    suspend operator fun invoke(
        userId: String,
        userToken: String,
        gameId: String
    ): Flow<Result<String>> {
        return gameRepository.generateGameDownloadLink(
            userId = userId,
            userToken = userToken,
            gameId = gameId
        )
    }
}
