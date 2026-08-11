package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewCatalogStateTest {

    private val fixture = GamePreviewViewModelTestFixture()
    private val gameRepository get() = fixture.gameRepository
    private val logger get() = fixture.logger

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    private fun createViewModel(
        gameId: String = TestFixtures.GAME_ID,
        connectedUser: Boolean = false,
    ) = fixture.createViewModel(gameId, connectedUser)

    private fun activateStateFlows(
        scope: CoroutineScope,
        viewModel: GamePreviewViewModel,
    ) = fixture.activateStateFlows(scope, viewModel)

    @Test
    fun `game emits Loading initially and Data when catalog emits`() = runTest {
        val viewModel = createViewModel()

        viewModel.game.test {
            assertEquals(GamePreviewUiState.Loading, awaitItem())

            gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
            assertTrue(awaitItem() is GamePreviewUiState.Data)
        }
    }

    @Test
    fun `game emits NotFound when catalog is null`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertTrue(logger.warnings.isEmpty())
    }

    @Test
    fun `game emits NotFound after start logs warning`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertTrue(logger.warnings.any { it.message.contains("not found locally") })
    }

    @Test
    fun `NotFound then recovery success self-heals to Data`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.success(TestFixtures.gameCatalog())
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            // NotFound may be conflated away by the StateFlow; the healed state must be Data.
            assertTrue(expectMostRecentItem() is GamePreviewUiState.Data)
        }
        assertEquals(1, gameRepository.getGameCatalogCalls)
    }

    @Test
    fun `NotFound recovery failure keeps NotFound and attempts repository once`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
            expectNoEvents()
        }
        assertEquals(1, gameRepository.getGameCatalogCalls)
        assertTrue(logger.warnings.any { it.message.contains("remote recovery failed") })
    }

    @Test
    fun `refresh on NotFound triggers one more recovery attempt`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(1, gameRepository.getGameCatalogCalls)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, gameRepository.getGameCatalogCalls)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `start refreshes catalog from remote once data is shown`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertTrue(expectMostRecentItem() is GamePreviewUiState.Data)
        }
        assertEquals(1, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `start does not refresh catalog remotely while catalog is missing`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertEquals(0, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `remote catalog refresh failure keeps cached data`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        gameRepository.refreshGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertTrue(expectMostRecentItem() is GamePreviewUiState.Data)
        }
        assertEquals(1, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `refresh on Data refreshes catalog from remote once more`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(1, gameRepository.refreshGameCatalogCalls)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, gameRepository.refreshGameCatalogCalls)
        assertFalse(viewModel.isRefreshing.value)
    }
}
