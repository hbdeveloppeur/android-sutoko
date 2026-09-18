package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Selecting the current chapter preserves progress and memories. Switching to a
 * different chapter updates progress. The engine restores earlier memories when it loads it.
 *
 * The existing hero name is preserved in the progress record.
 */
class SelectChapterUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
    private val transaction: GameProgressTransaction,
) {
    suspend operator fun invoke(gameId: String, chapterCode: String): Result<Unit> = try {
        transaction.run {
            val currentProgress = userGameProgressRepository.get(gameId)
            if (!currentProgress.currentChapterCode.equals(chapterCode, ignoreCase = true)) {
                userGameProgressRepository.save(
                    UserGameProgress(
                        gameId = gameId,
                        currentChapterCode = chapterCode,
                        normalizedChapterCode = chapterCode.lowercase(),
                        heroName = currentProgress.heroName,
                    )
                )
            }
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
