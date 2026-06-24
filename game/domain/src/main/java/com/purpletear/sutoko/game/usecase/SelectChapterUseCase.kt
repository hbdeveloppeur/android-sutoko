package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import javax.inject.Inject

/**
 * Updates the user's current chapter to [chapterCode] and wipes persisted memories
 * so the selected chapter starts from a clean state.
 *
 * The existing hero name is preserved in the progress record.
 */
class SelectChapterUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
    private val memoryRepository: MemoryRepository,
) {
    suspend operator fun invoke(gameId: String, chapterCode: String): Result<Unit> = runCatching {
        val currentProgress = userGameProgressRepository.get(gameId)
        userGameProgressRepository.save(
            UserGameProgress(
                gameId = gameId,
                currentChapterCode = chapterCode,
                normalizedChapterCode = chapterCode.lowercase(),
                heroName = currentProgress.heroName,
            )
        )
        memoryRepository.delete(gameId)
    }
}
