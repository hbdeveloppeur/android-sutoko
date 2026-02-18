package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for observing the current chapter for a specific game.
 */
class ObserveCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Invoke the use case to observe the current chapter for a specific game.
     *
     * @param gameId The ID of the game to observe the current chapter for.
     * @return A StateFlow emitting the current Chapter or null if no current chapter is set.
     */
    operator fun invoke(gameId: Int): StateFlow<Chapter?> {
        return chapterRepository.observeCurrentChapter(gameId)
    }
}