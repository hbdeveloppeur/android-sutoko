package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving user-created games with pagination.
 */
class GetUserGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to get user games.
     *
     * @param languageCode The language code (e.g., "fr-FR")
     * @param page The page number (starting from 1)
     * @param limit The number of items per page
     * @return A Flow emitting a Result containing a list of Games.
     */
    operator fun invoke(
        languageCode: String = "fr-FR",
        page: Int = 1,
        limit: Int = 20,
    ): Flow<Result<List<Game>>> {
        return gameRepository.getUsersGames(
            languageCode = languageCode,
            page = page,
            limit = limit
        )
    }
}
