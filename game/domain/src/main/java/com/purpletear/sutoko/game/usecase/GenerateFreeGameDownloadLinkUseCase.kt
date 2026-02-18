package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to generate a download link for a game.
 */
class GenerateFreeGameDownloadLinkUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Generate a download link for a game.
     *
     * @param gameId The ID of the game to download
     * @throws GameDownloadForbiddenException if the game download is forbidden
     * @return A Flow containing the Result of the operation with the download link as a String
     */
    suspend operator fun invoke(
        gameId: Int
    ): Flow<Result<String>> {
        return gameRepository.generateFreeGameDownloadLink(
            gameId = gameId
        )
    }
}