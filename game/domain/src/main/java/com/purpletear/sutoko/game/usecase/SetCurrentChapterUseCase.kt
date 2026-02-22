package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case for setting the current chapter for a specific game.
 */
class SetCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Invoke the use case to set the current chapter for a specific game.
     *
     * @param gameId The ID of the game to set the current chapter for.
     * @param chapter The chapter to set as current.
     */
    suspend operator fun invoke(gameId: String, chapter: Chapter) {
        chapterRepository.setCurrentChapter(gameId, chapter)
    }
}
