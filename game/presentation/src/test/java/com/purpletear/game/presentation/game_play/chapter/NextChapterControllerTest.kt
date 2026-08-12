package com.purpletear.game.presentation.game_play.chapter

import app.cash.turbine.test
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextChapterControllerTest {

    private val gameId = "game-id"

    @Test
    fun `available next chapter resolves state and navigates on click`() = runTest {
        val repository = FakeChapterRepository()
        repository.setChapters(
            gameId,
            Result.success(listOf(chapter(code = "2a", available = true, releaseDate = 1234L)))
        )
        var state = GameUiState()
        val controller = NextChapterController(gameId, repository, FakeLogger(), this) { transform ->
            state = transform(state)
        }

        controller.onChapterChange("2A")
        assertFalse(state.isNextChapterAvailabilityResolved)
        advanceUntilIdle()

        assertTrue(state.isNextChapterAvailabilityResolved)
        assertTrue(state.isNextChapterAvailable)
        assertEquals(1234L, state.nextChapterReleaseDate)
        controller.navigateToNextChapter.test {
            controller.onNextChapterClicked(state.isNextChapterAvailable)
            assertEquals("2A", awaitItem())
        }
    }

    @Test
    fun `missing next chapter fails closed and click sends no navigation`() = runTest {
        var state = GameUiState()
        val controller = NextChapterController(gameId, FakeChapterRepository(), FakeLogger(), this) { transform ->
            state = transform(state)
        }

        controller.onChapterChange("missing")
        advanceUntilIdle()

        assertTrue(state.isNextChapterAvailabilityResolved)
        assertFalse(state.isNextChapterAvailable)
        assertEquals(null, state.nextChapterReleaseDate)
        controller.navigateToNextChapter.test {
            controller.onNextChapterClicked(state.isNextChapterAvailable)
            expectNoEvents()
        }
    }

    @Test
    fun `failed availability lookup fails closed`() = runTest {
        val logger = FakeLogger()
        var state = GameUiState()
        val controller = NextChapterController(gameId, FailingChapterRepository(), logger, this) { transform ->
            state = transform(state)
        }

        controller.onChapterChange("2A")
        advanceUntilIdle()

        assertTrue(state.isNextChapterAvailabilityResolved)
        assertFalse(state.isNextChapterAvailable)
        assertEquals(1, logger.exceptions.size)
    }

    private fun chapter(code: String, available: Boolean, releaseDate: Long) = Chapter(
        code = code,
        available = available,
        releaseDate = releaseDate
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
