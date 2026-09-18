package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserGameProgress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
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
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 1, code = "1A", available = true),
        )
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
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 1, code = "1A", available = true),
        )
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

                assertEquals(GamePreviewEvent.ShowError(GameUiError.NickName), awaitItem())
                expectNoEvents()
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
    fun `required nickname is collected when resuming a later chapter`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 3, code = "3B", available = true))
        fixture.userGameProgressRepository.save(
            UserGameProgress(
                gameId = TestFixtures.GAME_ID,
                currentChapterCode = "3B",
                normalizedChapterCode = "3b",
            ),
        )
        val viewModel = createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnPlay)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.RequestNickName(isTrial = false), awaitItem())
            expectNoEvents()

            viewModel.onNickNameConfirmed("Alex", isTrial = false)
            advanceUntilIdle()
            val event = awaitItem() as GamePreviewEvent.PlayGame
            assertEquals("3b", event.chapterCode)
            assertFalse(event.isTrial)
            assertEquals("3b", fixture.userGameProgressRepository.get(TestFixtures.GAME_ID).normalizedChapterCode)
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
    @Test
    fun `save failure blocks gameplay and permits nickname retry`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 1, code = "1A", available = true),
        )
        val viewModel = createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()
        fixture.userGameProgressRepository.saveError = IllegalStateException("disk")

        viewModel.events.test {
            viewModel.onNickNameConfirmed("Alex", isTrial = true)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.ShowError(GameUiError.NickName), awaitItem())
            expectNoEvents()
            assertEquals("", fixture.userGameProgressRepository.get(TestFixtures.GAME_ID).heroName)

            fixture.userGameProgressRepository.saveError = null
            viewModel.onNickNameConfirmed("Alex", isTrial = true)
            advanceUntilIdle()
            assertTrue((awaitItem() as GamePreviewEvent.PlayGame).isTrial)
        }
    }

    @Test
    fun `saved nickname skips prompt and deleted progress prompts again`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        fixture.saveUserNickNameUseCase(TestFixtures.GAME_ID, "Alex")
        val viewModel = createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnTry)
            advanceUntilIdle()
            assertTrue(awaitItem() is GamePreviewEvent.PlayGame)
            assertEquals("Alex", fixture.userGameProgressRepository.get(TestFixtures.GAME_ID).heroName)

            fixture.userGameProgressRepository.delete(TestFixtures.GAME_ID)
            viewModel.onResume()
            viewModel.onAction(GamePreviewAction.OnTry)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.RequestNickName(isTrial = true), awaitItem())
        }
    }

    @Test
    fun `nickname read failure blocks gameplay`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        val viewModel = createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()
        fixture.userGameProgressRepository.getError = IllegalStateException("disk")

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnTry)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.ShowError(GameUiError.NickName), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `friendzoned mirror failure blocks gameplay and next play retries saved name`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(legacyId = 162, userNickNameRequired = true))
        gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        val viewModel = createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()
        friendzonedProgressRepository.saveNameError = IllegalStateException("disk")

        viewModel.events.test {
            viewModel.onNickNameConfirmed("Alex", isTrial = true)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.ShowError(GameUiError.NickName), awaitItem())
            expectNoEvents()

            friendzonedProgressRepository.saveNameError = null
            viewModel.onAction(GamePreviewAction.OnTry)
            advanceUntilIdle()
            assertTrue((awaitItem() as GamePreviewEvent.PlayGame).isTrial)
            assertEquals("Alex", friendzonedProgressRepository.firstNames[162])
        }
    }

    @Test
    fun `repeated nickname confirmation saves and navigates once`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 1, code = "1A", available = true),
        )
        val viewModel = createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()
        val release = CompletableDeferred<Unit>()
        fixture.userGameProgressRepository.beforeSave = { release.await() }

        viewModel.events.test {
            viewModel.onNickNameConfirmed("Alex", isTrial = true)
            advanceUntilIdle()
            assertTrue(viewModel.isSavingNickName.value)
            viewModel.onNickNameConfirmed("Other", isTrial = true)
            advanceUntilIdle()
            assertEquals(1, fixture.userGameProgressRepository.saveCalls)
            expectNoEvents()

            release.complete(Unit)
            advanceUntilIdle()
            assertTrue(awaitItem() is GamePreviewEvent.PlayGame)
            expectNoEvents()
            assertFalse(viewModel.isSavingNickName.value)
            assertEquals("Alex", fixture.userGameProgressRepository.get(TestFixtures.GAME_ID).heroName)
        }
    }

}
