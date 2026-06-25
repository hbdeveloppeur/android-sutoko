package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Use case for downloading a game.
 * Encapsulates business rules: authentication validation for premium games
 * and download initiation.
 */
class DownloadGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val gameInstallRepository: GameInstallRepository,
) {

    suspend operator fun invoke(
        gameId: String,
        userId: String?,
        userToken: String?,
    ): Flow<Float> {
        assert(gameId.isNotBlank(), { "gameId must not be blank" })
        assert(userId == null || userId.isNotBlank(), { "userId must not be blank" })
        assert(userToken == null || userToken.isNotBlank(), { "userToken must not be blank" })


        val game = gameRepository.observeGame(gameId).firstOrNull()
            ?: throw IllegalArgumentException("Game not found: $gameId")

        val downloadUrl = gameRepository.getDownloadLink(
            gameId = gameId,
            userId = userId,
            userToken = userToken,
        ).getOrThrow()

        return gameInstallRepository.download(
            gameId = gameId,
            gameDownloadUrl = downloadUrl,
            gameVersion = game.version.toString(),
            legacyId = game.legacyId,
        )
    }
}
