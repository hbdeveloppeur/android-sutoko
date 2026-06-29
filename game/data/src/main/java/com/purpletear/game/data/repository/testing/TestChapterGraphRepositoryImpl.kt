package com.purpletear.game.data.repository.testing

import com.purpletear.game.data.graph.testing.TestChapterGraphLoader
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.repository.testing.TestChapterGraphRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestChapterGraphRepositoryImpl @Inject constructor(
    private val loader: TestChapterGraphLoader,
) : TestChapterGraphRepository {

    override fun load(extractedDirectory: String, gameId: String): Result<ChapterGraph> {
        return runCatching {
            loader.load(extractedDirectory, gameId)
        }
    }
}
