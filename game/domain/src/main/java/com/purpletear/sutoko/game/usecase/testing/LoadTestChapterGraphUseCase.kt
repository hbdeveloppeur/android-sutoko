package com.purpletear.sutoko.game.usecase.testing

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.testing.TestChapterGraphRepository
import javax.inject.Inject

class LoadTestChapterGraphUseCase @Inject constructor(
    private val repository: TestChapterGraphRepository,
) {
    operator fun invoke(extractedDirectory: String, gameId: String): Result<ChapterGraph> {
        return repository.load(extractedDirectory, gameId)
    }
}
