package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking if a game is updatable.
 */
class HasGameLocalFilesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Invoke the use case to check if a game is updatable.
     *
     * @param game The game to check for updates.
     * @return True if the game is updatable, false otherwise.
     */
    suspend operator fun invoke(game: Game): Flow<Result<Boolean>> {
        return gameRepository.hasGameLocalFiles(game)
    }
}