package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    operator fun invoke(gameId: String): Flow<Chapter?> {
        return chapterRepository.observeCurrentChapter(gameId)
    }
}
