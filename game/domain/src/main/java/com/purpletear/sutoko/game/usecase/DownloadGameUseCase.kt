package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.game.GameDownloadRequest
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
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        gameId: String,
    ): Flow<Float> {
        assert(gameId.isNotBlank(), { "gameId must not be blank" })

        return gameInstallRepository.download(gameId) {
            val user = userRepository.observeUser().firstOrNull()
            val game = gameRepository.observeGame(gameId).firstOrNull()
                ?: throw IllegalArgumentException("Game not found: $gameId")
            val downloadUrl = gameRepository.getDownloadLink(
                gameId = gameId,
                userId = user?.id,
                userToken = user?.token,
            ).getOrThrow()
            GameDownloadRequest(downloadUrl, game.version.toString(), game.legacyId)
        }
    }
}
