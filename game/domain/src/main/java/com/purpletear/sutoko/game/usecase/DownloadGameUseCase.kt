package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.domain.repository.UserRepository
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

    /**
     * @param preview Admin-only: download the preview archive (all chapters,
     * including unreleased ones). Requires a logged-in admin; the download
     * always re-downloads (no version cache) since preview builds never bump
     * the story version.
     */
    suspend operator fun invoke(
        gameId: String,
        preview: Boolean = false,
    ): Flow<Float> {
        assert(gameId.isNotBlank(), { "gameId must not be blank" })

        val user = userRepository.observeUser().firstOrNull()

        if (preview) {
            requireNotNull(user?.token) { "Preview download requires a logged-in user" }
        }

        val game = gameRepository.observeGame(gameId).firstOrNull()
            ?: throw IllegalArgumentException("Game not found: $gameId")

        val downloadUrl = gameRepository.getDownloadLink(
            gameId = gameId,
            // Preview links only need the admin token, not the user id.
            userId = if (preview) null else user?.id,
            userToken = user?.token,
            preview = preview,
        ).getOrThrow()

        return gameInstallRepository.download(
            gameId = gameId,
            gameDownloadUrl = downloadUrl,
            gameVersion = game.version.toString(),
            legacyId = game.legacyId,
        )
    }
}
