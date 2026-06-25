package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import javax.inject.Inject

/**
 * Load the stories authored by a specific user.
 */
class GetOneUserGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * @param userId The id of the author whose stories are requested.
     * @param page Page number, starting from 1.
     * @param limit Number of items per page.
     */
    suspend operator fun invoke(
        userId: String,
        page: Int = 1,
        limit: Int = 20,
    ): Result<List<GameCatalog>> = gameRepository.getOneUserGames(userId, page, limit)
}
