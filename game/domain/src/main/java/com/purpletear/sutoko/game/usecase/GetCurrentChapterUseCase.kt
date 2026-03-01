package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) {
    operator fun invoke(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> {
        return chapterRepository.getCurrentChapter(gameId, forceReload)
    }
}
