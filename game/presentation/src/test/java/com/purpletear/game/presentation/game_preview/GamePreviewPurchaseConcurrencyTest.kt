package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import kotlinx.coroutines.CompletableDeferred
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
class GamePreviewPurchaseConcurrencyTest {
    private val fixture = GamePreviewViewModelTestFixture()

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    @Test
    fun `repeated confirmations share one request and failure allows retry`() = runTest {
        fixture.gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")),
        )
        val release = CompletableDeferred<Unit>()
        val purchase = fixture.buyStoryWithCoinsUseCase
        purchase.beforePurchase = { release.await() }
        purchase.setResult("sku-1", Result.failure(IllegalStateException("network")))
        val viewModel = fixture.createViewModel(connectedUser = true)
        fixture.activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()
            assertEquals(listOf("sku-1"), purchase.calls)
            assertTrue(viewModel.isPurchaseLoading.value)

            viewModel.onAction(GamePreviewAction.OnAbortBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()
            assertEquals(1, purchase.calls.size)
            assertTrue(viewModel.isPurchaseLoading.value)
            expectNoEvents()

            release.complete(Unit)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.ShowError(GameUiError.Purchase), awaitItem())
            assertFalse(viewModel.isPurchaseLoading.value)

            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()
            assertEquals(2, purchase.calls.size)
            assertEquals(GamePreviewEvent.ShowError(GameUiError.Purchase), awaitItem())
        }
    }
    @Test
    fun `confirmation from a closed dialog cannot purchase again`() = runTest {
        fixture.gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")),
        )
        val viewModel = fixture.createViewModel(connectedUser = true)
        fixture.activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.PurchaseSuccess, awaitItem())
            assertFalse(viewModel.isPurchasing.value)

            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()
            assertEquals(listOf("sku-1"), fixture.buyStoryWithCoinsUseCase.calls)
            expectNoEvents()
        }
    }

}
