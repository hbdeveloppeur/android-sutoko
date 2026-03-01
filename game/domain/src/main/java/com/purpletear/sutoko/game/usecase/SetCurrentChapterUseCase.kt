package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import javax.inject.Inject

class SetCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    suspend operator fun invoke(gameId: String, chapter: Chapter) {
        chapterRepository.setCurrentChapter(gameId, chapter)
    }
}
