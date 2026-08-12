package com.purpletear.game.presentation.game_play.layout

import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RightSideLayoutResolverTest {

    private val gameId = "game-id"
    private val chapterCode = "1A"

    @Test
    fun `archive layout wins over room layout`() = runTest {
        val repository = FakeChapterRepository()
        repository.setChapters(
            gameId,
            Result.success(listOf(chapter(code = "1a", rightSideIds = listOf(3))))
        )
        val resolver = RightSideLayoutResolver(repository, FakeLogger())

        val archiveIds = resolver.onGraphLoaded(graph(rightSideIds = listOf(1, 2)))
        val observedIds = resolver.observeRoomLayout(gameId, chapterCode).first()

        assertEquals(setOf(1, 2), archiveIds)
        assertEquals(setOf(1, 2), observedIds)
    }

    @Test
    fun `room layout is used when archive declares no sides`() = runTest {
        val repository = FakeChapterRepository()
        repository.setChapters(
            gameId,
            Result.success(listOf(chapter(code = "1a", rightSideIds = listOf(3, 4))))
        )
        val resolver = RightSideLayoutResolver(repository, FakeLogger())
        resolver.onGraphLoaded(graph(rightSideIds = emptyList()))

        val observedIds = resolver.observeRoomLayout(gameId, chapterCode).first()

        assertEquals(setOf(3, 4), observedIds)
    }

    @Test
    fun `empty layout lets screen use legacy rule`() = runTest {
        val resolver = RightSideLayoutResolver(FakeChapterRepository(), FakeLogger())

        val observedIds = resolver.observeRoomLayout(gameId, chapterCode).first()

        assertEquals(emptySet<Int>(), observedIds)
    }

    @Test
    fun `room observation failure falls back to empty layout`() = runTest {
        val logger = FakeLogger()
        val resolver = RightSideLayoutResolver(FailingChapterRepository(), logger)

        val observedIds = resolver.observeRoomLayout(gameId, chapterCode).first()

        assertEquals(emptySet<Int>(), observedIds)
        assertEquals(1, logger.exceptions.size)
    }

    private fun chapter(code: String, rightSideIds: List<Int>) = Chapter(
        code = code,
        rightSideCharacterIds = rightSideIds
    )

    private fun graph(rightSideIds: List<Int>) = ChapterGraph(
        chapterCode = chapterCode,
        title = "Test",
        nodes = mapOf("start" to Node.Start(id = "start")),
        edges = emptyList(),
        startNodeId = "start",
        rightSideCharacterIds = rightSideIds
    )

    private class FailingChapterRepository : ChapterRepository {
        private val error = IllegalStateException("database unavailable")

        override fun getChapters(storyId: String): Flow<Result<List<Chapter>>> = flow { throw error }
        override fun observeChapters(storyId: String): Flow<List<Chapter>> = flow { throw error }
        override fun observeStoryIdsWithUpcomingChapters(): Flow<Set<String>> = flowOf(emptySet())
        override fun getChapter(id: Int): Flow<Result<Chapter>> = flow { throw error }
        override fun getCurrentChapter(gameId: String, forceReload: Boolean): Flow<Result<Chapter?>> =
            flow { throw error }
        override fun observeCurrentChapter(gameId: String): Flow<Chapter?> = flow { throw error }
    }
}
