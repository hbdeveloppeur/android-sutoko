package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving the current chapter for a specific game.
 */
class GetCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Invoke the use case to get the current chapter for a specific game.
     *
     * @param gameId The ID of the game to retrieve the current chapter for.
     * @return A Flow emitting the current Chapter or null if no current chapter is set.
     */
    operator fun invoke(gameId: Int, forceReload: Boolean): Flow<Result<Chapter?>> {
        return chapterRepository.getCurrentChapter(gameId, forceReload)
    }
}