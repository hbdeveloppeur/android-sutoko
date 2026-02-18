package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for setting the game version.
 */
class SetGameVersionUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to set the game version.
     *
     * @param game The game for which to set the version.
     * @return A Flow containing the Result of the operation.
     */
    suspend operator fun invoke(game: Game): Flow<Result<Unit>> {
        return gameRepository.setGameVersion(game)
    }
}