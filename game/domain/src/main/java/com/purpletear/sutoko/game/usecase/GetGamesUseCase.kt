package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all games.
 */
class GetGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to get all games.
     *
     * @return A Flow emitting a Result containing a list of Games.
     */
    operator fun invoke(): Flow<Result<List<Game>>> {
        return gameRepository.getOfficialGames()
    }
}
