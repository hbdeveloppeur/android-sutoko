package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewNicknameNavigationTest {

    private val fixture = GamePreviewViewModelTestFixture()
    private val gameRepository get() = fixture.gameRepository
    private val chapterRepository get() = fixture.chapterRepository
    private val gameInstallRepository get() = fixture.gameInstallRepository
    private val friendzonedProgressRepository get() = fixture.friendzonedProgressRepository
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
    fun `onNickNameConfirmed with friendzoned game mirrors the name to the friendzoned store`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(legacyId = 162, userNickNameRequired = true),
        )
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onNickNameConfirmed("Alex", isTrial = false)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                assertEquals(162, (event as GamePreviewEvent.PlayGame).legacyId)
            }
        }
        assertEquals("Alex", friendzonedProgressRepository.firstNames[162])
    }

    @Test
    fun `onNickNameConfirmed with standard game does not touch the friendzoned store`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onNickNameConfirmed("Alex", isTrial = false)
                advanceUntilIdle()

                assertTrue(awaitItem() is GamePreviewEvent.PlayGame)
            }
        }
        assertTrue(friendzonedProgressRepository.firstNames.isEmpty())
    }

    @Test
    fun `onNickNameConfirmed with invalid name does not touch the friendzoned store`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(legacyId = 162, userNickNameRequired = true),
        )
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onNickNameConfirmed("Al", isTrial = false) // below min length: rejected
                advanceUntilIdle()

                assertTrue(awaitItem() is GamePreviewEvent.PlayGame)
            }
        }
        assertTrue(friendzonedProgressRepository.firstNames.isEmpty())
    }

    @Test
    fun `onAction OnTry with nickname required keeps isTrial after confirm`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(
                price = 100,
                skus = listOf("sku-1"),
                userNickNameRequired = true,
            ),
        )
        gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnTry)
                advanceUntilIdle()
                val request = awaitItem()
                assertTrue(request is GamePreviewEvent.RequestNickName)
                request as GamePreviewEvent.RequestNickName
                assertTrue(request.isTrial)

                viewModel.onNickNameConfirmed("Alex", isTrial = request.isTrial)
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
    fun `onNickNameConfirmed with null chapter does not navigate`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        // No setCurrentChapter: the chapter vanished between the nickname
        // request and its confirmation - the boundary guard must hold.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onNickNameConfirmed("Alex", isTrial = false)
                advanceUntilIdle()

                assertEquals(GamePreviewEvent.ShowError(GameUiError.Load), awaitItem())
            }
        }
        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_error_load_game))
    }
}
