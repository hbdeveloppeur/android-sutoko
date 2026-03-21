package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import javax.inject.Inject

class RestartGameUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
    private val memoryRepository: MemoryRepository,
) {
    /**
     * Deletes the user's game progress to restart from the beginning.
     * Returns Result.success on completion, or Result.failure with the exception on error.
     */
    suspend operator fun invoke(gameId: String): Result<Unit> {
        return try {
            // Delete user progress.
            userGameProgressRepository.delete(gameId = gameId)
            // Delete memories
            memoryRepository.delete(gameId = gameId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
