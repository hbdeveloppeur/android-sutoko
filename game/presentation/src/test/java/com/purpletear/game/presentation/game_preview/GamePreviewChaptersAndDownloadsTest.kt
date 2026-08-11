package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewChaptersAndDownloadsTest {

    private val fixture = GamePreviewViewModelTestFixture()
    private val gameRepository get() = fixture.gameRepository
    private val chapterRepository get() = fixture.chapterRepository
    private val gameInstallRepository get() = fixture.gameInstallRepository
    private val toastService get() = fixture.toastService
    private val logger get() = fixture.logger

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    private fun createViewModel(
        gameId: String = TestFixtures.GAME_ID,
        connectedUser: Boolean = false,
    ) = fixture.createViewModel(gameId, connectedUser)

    @Test
    fun `onAction OnDownload starts download and emits no error`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
            gameRepository.setDownloadLink(TestFixtures.GAME_ID, Result.success("https://example.com/download"))
            gameInstallRepository.setDownloadFlow(TestFixtures.GAME_ID, flowOf(0.5f, 1.0f))
            advanceUntilIdle()

            viewModel.onAction(GamePreviewAction.OnDownload)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `start loads chapters and emits ShowError on failure`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            val error = RuntimeException("chapters failed")
            chapterRepository.setChapters(TestFixtures.GAME_ID, Result.failure(error))

            viewModel.start()
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowError(GameUiError.Load), awaitItem())
        }
        assertTrue(logger.exceptions.any { it.throwable.message == "chapters failed" })
    }

    @Test
    fun `start loads empty chapters and logs warning`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))

            viewModel.start()
            advanceUntilIdle()

            expectNoEvents()
        }
        assertTrue(logger.warnings.any { it.message.contains("empty chapter list") })
    }

    @Test
    fun `refresh reloads chapters and resets isRefreshing`() = runTest {
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(0, gameRepository.syncOfficialGamesCalls)
        assertEquals(1, chapterRepository.getChaptersCalls)
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(toastService.shownMessages.isEmpty())
    }

    @Test
    fun `refresh shows toast and resets isRefreshing on failure`() = runTest {
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.failure(RuntimeException("chapters failed")))
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_error_load_game))
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(logger.warnings.any { it.message.contains("refresh failed") })
    }

    @Test
    fun `refresh while already refreshing is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        chapterRepository.getChaptersGate = gate
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.isRefreshing.value)

        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(1, chapterRepository.getChaptersCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `releasedChaptersCount is null when no chapters are stored`() = runTest {
        val viewModel = createViewModel()

        viewModel.releasedChaptersCount.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `releasedChaptersCount counts only available chapters`() = runTest {
        val viewModel = createViewModel()
        val now = System.currentTimeMillis() / 1000

        viewModel.releasedChaptersCount.test {
            assertEquals(null, awaitItem())

            chapterRepository.setChapters(
                TestFixtures.GAME_ID,
                Result.success(
                    listOf(
                        Chapter(id = "1", number = 1, releaseDate = now - 200, available = true),
                        Chapter(id = "2", number = 2, releaseDate = now - 100, available = true),
                        Chapter(id = "3", number = 3, releaseDate = now + 100_000),
                    )
                )
            )

            assertEquals(2, awaitItem())
        }
    }
}
