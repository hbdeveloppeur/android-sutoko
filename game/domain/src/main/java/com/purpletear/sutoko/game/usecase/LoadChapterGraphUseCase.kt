package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.ChapterGraphRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadChapterGraphUseCase @Inject constructor(
    private val repository: ChapterGraphRepository
) {
    operator fun invoke(gameId: String, chapterCode: String, language: String = "fr-FR"): Flow<Result<ChapterGraph>> {
        return repository.loadChapterGraph(gameId, chapterCode, language)
    }
}
