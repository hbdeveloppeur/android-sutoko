package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    operator fun invoke(gameId: String): StateFlow<Chapter?> {
        return chapterRepository.observeCurrentChapter(gameId)
    }
}
