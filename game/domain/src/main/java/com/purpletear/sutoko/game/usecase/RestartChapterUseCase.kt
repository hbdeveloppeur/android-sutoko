package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class RestartChapterUseCase @Inject constructor(
    private val progressRepository: UserGameProgressRepository,
    private val memoryRepository: MemoryRepository,
    private val transaction: GameProgressTransaction,
) {
    suspend operator fun invoke(gameId: String, chapter: Chapter): Result<Unit> = try {
        require(chapter.story == gameId && chapter.number > 0 && chapter.code.isNotBlank())
        transaction.run {
            val progress = progressRepository.get(gameId)
            check(progress.currentChapterCode.equals(chapter.code, ignoreCase = true)) {
                "Current chapter changed before restart"
            }
            // Loading the chapter's starting memories removes this and later chapters' writes.
            memoryRepository.load(gameId, chapter.number)
            progressRepository.save(progress.copy(checkpoint = null))
        }
        Result.success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
