package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.FriendzonedLegacyIds
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class RestartGameUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
    private val memoryRepository: MemoryRepository,
    private val friendzonedProgressRepository: FriendzonedProgressRepository,
) {
    /**
     * Deletes the user's game progress to restart from the beginning.
     * Returns Result.success on completion, or Result.failure with the exception on error.
     *
     * [legacyId] is the game's legacy identifier when known. Friendzoned games
     * keep their progress in their own store, which is reset too; for every
     * other game (null or non-Friendzoned id) behavior is unchanged.
     */
    suspend operator fun invoke(gameId: String, legacyId: Int? = null): Result<Unit> {
        return try {
            // Delete user progress.
            userGameProgressRepository.delete(gameId = gameId)
            // Delete memories
            memoryRepository.delete(gameId = gameId)
            // Friendzoned games manage their progress themselves: reset it too.
            if (FriendzonedLegacyIds.isFriendzoned(legacyId)) {
                friendzonedProgressRepository.reset(legacyId!!)
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
