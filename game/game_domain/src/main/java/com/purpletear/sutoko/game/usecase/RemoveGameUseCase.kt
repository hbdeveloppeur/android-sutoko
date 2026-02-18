package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for removing a game.
 */
class RemoveGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to remove a game.
     *
     * @param game The game to remove.
     * @return A Flow containing the Result of the operation.
     */
    suspend operator fun invoke(game: Game): Flow<Result<Unit>> {
        return gameRepository.removeGame(game)
    }
}