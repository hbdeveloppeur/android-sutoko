package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a specific game by ID.
 */
class GetGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to get a specific game by ID.
     *
     * @param id The ID of the game to retrieve.
     * @return A Flow emitting a Result containing the requested Game.
     */
    operator fun invoke(id: String): Flow<Result<Game>> {
        return gameRepository.getGame(id)
    }
}
