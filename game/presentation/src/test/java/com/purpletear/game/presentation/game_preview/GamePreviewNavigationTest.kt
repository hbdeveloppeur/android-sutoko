package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewNavigationTest {

    private val fixture = GamePreviewViewModelTestFixture()
    private val gameRepository get() = fixture.gameRepository
    private val chapterRepository get() = fixture.chapterRepository
    private val gameInstallRepository get() = fixture.gameInstallRepository
    private val userRoleRepository get() = fixture.userRoleRepository
    private val toastService get() = fixture.toastService

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    private fun createViewModel(
        gameId: String = TestFixtures.GAME_ID,
        connectedUser: Boolean = false,
    ) = fixture.createViewModel(gameId, connectedUser)

    @Test
    fun `onAction OnTry emits PlayGame with isTrial and chapter code`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        // Keep currentChapter active so its StateFlow value is populated (as in the real screen).
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data) // game.value is now Data

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnTry)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                event as GamePreviewEvent.PlayGame
                assertTrue(event.isTrial)
                assertEquals("1a", event.chapterCode)
            }
        }
    }

    @Test
    fun `onAction OnTry without install downloads then navigates to play`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        gameRepository.setDownloadLink(TestFixtures.GAME_ID, Result.success("https://example.com/game.zip"))
        gameInstallRepository.setDownloadFlow(TestFixtures.GAME_ID, flowOf(0.5f, 1f))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnTry)
                advanceUntilIdle()

                // Regression: trial used to navigate with no files on disk and die
                // on "No chapter language found". Now the download runs first.
                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                event as GamePreviewEvent.PlayGame
                assertTrue(event.isTrial)
                assertEquals("1a", event.chapterCode)
            }
        }
    }

    @Test
    fun `onAction OnTry with failing download shows error and does not navigate`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        gameRepository.setDownloadLink(TestFixtures.GAME_ID, Result.failure(UnknownHostException("offline")))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        val events = mutableListOf<GamePreviewEvent>()
        backgroundScope.launch { viewModel.events.collect { events.add(it) } }
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnTry)
        advanceUntilIdle()

        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_error_download))
        assertTrue(events.none { it is GamePreviewEvent.PlayGame })
    }

    @Test
    fun `onAction OnPlay emits PlayGame with isTrial false`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        backgroundScope.launch { viewModel.currentChapter.collect { } }

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnPlay)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                assertFalse((event as GamePreviewEvent.PlayGame).isTrial)
            }
        }
    }

    @Test
    fun `onAction OnPlay with unavailable chapter shows toast and does not navigate`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 2, code = "1B", releaseDate = System.currentTimeMillis() / 1000 + 86_400),
        )
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        val events = mutableListOf<GamePreviewEvent>()
        backgroundScope.launch { viewModel.events.collect { events.add(it) } }
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnPlay)
        advanceUntilIdle()

        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_game_preview_next_chapter))
        assertTrue(events.none { it is GamePreviewEvent.PlayGame })
    }

    @Test
    fun `onAction OnPlay with unavailable chapter as admin still navigates`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 2, code = "1B", releaseDate = System.currentTimeMillis() / 1000 + 86_400),
        )
        userRoleRepository.set(UserRole.ADMINISTRATOR)
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        backgroundScope.launch { viewModel.isAdmin.collect { } }

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnPlay)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
            }
        }
        assertTrue(toastService.shownMessages.isEmpty())
    }

    @Test
    fun `onAction OnTry with null chapter shows error toast and does not navigate`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        // No setCurrentChapter: currentChapter stays null (chapter not loaded).
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        val events = mutableListOf<GamePreviewEvent>()
        backgroundScope.launch { viewModel.events.collect { events.add(it) } }
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnTry)
        advanceUntilIdle()

        // Regression: a null chapter used to reach SmsGameActivity and crash.
        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_error_load_game))
        assertTrue(events.none { it is GamePreviewEvent.PlayGame })
    }

    @Test
    fun `onAction OnTry with unavailable chapter shows toast and does not navigate`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 1, code = "1A", releaseDate = System.currentTimeMillis() / 1000 + 86_400),
        )
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        val events = mutableListOf<GamePreviewEvent>()
        backgroundScope.launch { viewModel.events.collect { events.add(it) } }
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnTry)
        advanceUntilIdle()

        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_game_preview_next_chapter))
        assertTrue(events.none { it is GamePreviewEvent.PlayGame })
    }
}
