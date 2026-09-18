package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runCurrent
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
    fun `missing cache stays Loading before recovery`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            assertEquals(GamePreviewUiState.Loading, awaitItem())
            runCurrent()
            expectNoEvents()
        }
        assertTrue(logger.warnings.isEmpty())
    }

    @Test
    fun `game emits NotFound only after remote confirms absence`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertEquals(1, gameRepository.getGameCatalogCalls)
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
    fun `recovery failure emits Error and attempts repository once`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.Error(GameUiError.Load), awaitItem())
            expectNoEvents()
        }
        assertEquals(1, gameRepository.getGameCatalogCalls)
        assertTrue(logger.exceptions.any { it.throwable.message == "network" })
    }

    @Test
    fun `catalog removed after loading is recovered once per removal`() = runTest {
        val catalog = TestFixtures.gameCatalog()
        gameRepository.setGame(TestFixtures.GAME_ID, catalog)
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)
        viewModel.start()
        advanceUntilIdle()
        assertTrue(viewModel.game.value is GamePreviewUiState.Data)

        val gate = CompletableDeferred<Unit>()
        gameRepository.catalogGate = gate
        gameRepository.getGameCatalogResult = Result.success(catalog)
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        advanceUntilIdle()
        assertEquals(1, gameRepository.getGameCatalogCalls)
        assertEquals(GamePreviewUiState.Loading, viewModel.game.value)

        gate.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.game.value is GamePreviewUiState.Data)

        gameRepository.getGameCatalogResult = Result.success(null)
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        advanceUntilIdle()
        assertEquals(GamePreviewUiState.NotFound, viewModel.game.value)
        assertEquals(2, gameRepository.getGameCatalogCalls)
        viewModel.start()
        advanceUntilIdle()
        assertEquals(2, gameRepository.getGameCatalogCalls)
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
    @Test
    fun `repeated start loads once while explicit refresh still reloads`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        viewModel.start()
        advanceUntilIdle()
        viewModel.start()
        advanceUntilIdle()
        assertEquals(1, gameRepository.refreshGameCatalogCalls)

        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(2, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `remote recovery remains Loading until the request answers`() = runTest {
        val gate = CompletableDeferred<Unit>()
        gameRepository.catalogGate = gate
        gameRepository.getGameCatalogResult = Result.success(TestFixtures.gameCatalog())
        val vm = createViewModel()
        activateStateFlows(backgroundScope, vm)
        vm.start()
        runCurrent()
        assertEquals(GamePreviewUiState.Loading, vm.game.value)
        gate.complete(Unit)
        advanceUntilIdle()
        assertTrue(vm.game.value is GamePreviewUiState.Data)
    }

    @Test
    fun `retry restarts an observation that previously failed`() = runTest {
        gameRepository.setError(TestFixtures.GAME_ID, IllegalStateException("database unavailable"))
        val vm = createViewModel()
        activateStateFlows(backgroundScope, vm)
        vm.start()
        runCurrent()
        assertEquals(GamePreviewUiState.Error(GameUiError.Load), vm.game.value)
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.game.value is GamePreviewUiState.Data)
    }

}
