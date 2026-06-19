package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import javax.inject.Inject

/**
 * Use case for removing a game.
 */
class RemoveGameUseCase @Inject constructor(
    private val gameInstallRepository: GameInstallRepository
) {
    suspend operator fun invoke(gameId: String): Result<Unit> {
        return gameInstallRepository.deleteGame(gameId)
    }
}