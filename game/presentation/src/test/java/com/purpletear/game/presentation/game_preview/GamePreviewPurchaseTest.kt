package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.domain.model.User
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
class GamePreviewPurchaseTest {

    private val fixture = GamePreviewViewModelTestFixture()
    private val gameRepository get() = fixture.gameRepository
    private val userRepository get() = fixture.userRepository
    private val entitlementRepository get() = fixture.entitlementRepository
    private val buyStoryWithCoinsUseCase get() = fixture.buyStoryWithCoinsUseCase
    private val shopRepository get() = fixture.shopRepository

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
    fun `onAction OnBuy when not connected emits OpenAccountConnection`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.OpenAccountConnection, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `onAction OnBuy when connected sets isPurchasing`() = runTest {
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnBuy)
        advanceUntilIdle()

        assertTrue(viewModel.isPurchasing.value)
        assertFalse(viewModel.isPurchaseLoading.value)
    }

    @Test
    fun `onAction OnBuyConfirm emits ShowError when no SKU`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowError(GameUiError.Purchase), awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `global premium makes a paid game owned`() = runTest {
        entitlementRepository.hasPremiumFlow.value = true
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            // The first Data may predate the entitlement emission; the most recent one wins.
            advanceUntilIdle()
            val data = expectMostRecentItem()
            assertTrue(data is GamePreviewUiState.Data)
            assertTrue((data as GamePreviewUiState.Data).item.isPurchased)
        }
    }

    @Test
    fun `paid game without sku and without premium is not owned`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            val data = awaitItem()
            assertTrue(data is GamePreviewUiState.Data)
            assertFalse((data as GamePreviewUiState.Data).item.isPurchased)
        }
    }

    @Test
    fun `server granted sku makes a paid game owned`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        entitlementRepository.isGrantedFlow.value = true
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            // The first Data may predate the entitlement emission; the most recent one wins.
            advanceUntilIdle()
            val data = expectMostRecentItem()
            assertTrue(data is GamePreviewUiState.Data)
            assertTrue((data as GamePreviewUiState.Data).item.isPurchased)
        }
    }

    @Test
    fun `coin grant check runs once when connected and story unbought`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()

        assertEquals(1, entitlementRepository.refreshGrantCalls)
    }

    @Test
    fun `coin grant check grants access when the server confirms the grant`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        entitlementRepository.refreshGrantResult = Result.success(true)
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()

        val data = viewModel.game.value as GamePreviewUiState.Data
        assertTrue(data.item.isPurchased)
    }

    @Test
    fun `coin grant check is deferred until the user connects`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(0, entitlementRepository.refreshGrantCalls)

        userRepository.setUser(User(id = "user-1", token = "token-1"))
        advanceUntilIdle()
        assertEquals(1, entitlementRepository.refreshGrantCalls)
    }

    @Test
    fun `coin grant check retries transient failures until a definitive answer`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        entitlementRepository.enqueueResults(
            listOf(
                Result.failure(RuntimeException("network")),
                Result.failure(RuntimeException("network")),
                Result.success(true),
            )
        )
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()

        assertEquals(3, entitlementRepository.refreshGrantCalls)
    }

    @Test
    fun `coin grant check gives up after bounded retries and refresh grants a fresh round`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        entitlementRepository.enqueueResults(
            listOf(
                Result.failure(RuntimeException("network")),
                Result.failure(RuntimeException("network")),
                Result.failure(RuntimeException("network")),
            )
        )
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(3, entitlementRepository.refreshGrantCalls)

        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(4, entitlementRepository.refreshGrantCalls)
    }

    @Test
    fun `successful coin purchase emits PurchaseSuccess`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.success(com.purpletear.sutoko.shop.domain.repository.model.Balance(coins = 900, diamonds = 0)))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.PurchaseSuccess, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `coin purchase already owned emits ShowAlreadyBoughtAlert`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.failure(com.purpletear.sutoko.shop.domain.error.BuyStoryError.AlreadyOwned()))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowAlreadyBoughtAlert, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `onAction OnBuy with insufficient balance emits OpenShop and skips the purchase flow`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        shopRepository.balanceFlow.value = com.purpletear.sutoko.shop.domain.repository.model.Balance(coins = 50, diamonds = 0)
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.OpenShop, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `onAction OnBuyConfirm with insufficient balance emits OpenShop`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        shopRepository.balanceFlow.value = com.purpletear.sutoko.shop.domain.repository.model.Balance(coins = 150, diamonds = 0)
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()
            assertTrue(viewModel.isPurchasing.value)

            // The balance drops while the confirmation dialog is open.
            shopRepository.balanceFlow.value = com.purpletear.sutoko.shop.domain.repository.model.Balance(coins = 10, diamonds = 0)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.OpenShop, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `coin purchase insufficient funds emits OpenShop`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.failure(com.purpletear.sutoko.shop.domain.error.BuyStoryError.InsufficientFunds()))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.OpenShop, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `coin purchase not purchasable emits ShowError`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.failure(com.purpletear.sutoko.shop.domain.error.BuyStoryError.NotPurchasable()))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowError(GameUiError.Purchase), awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }
}
