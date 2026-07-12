package com.purpletear.game.presentation.game_preview.handlers

import com.purpletear.game.presentation.game_preview.fakes.FakeBuyStoryWithCoinsUseCase
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePreviewPurchaseHandlerTest {

    private val buyStoryWithCoinsUseCase = FakeBuyStoryWithCoinsUseCase()
    private val handler = GamePreviewPurchaseHandler(buyStoryWithCoinsUseCase)

    @Test
    fun `startPurchaseFlow exposes isPurchasing`() = runTest {
        handler.startPurchaseFlow()

        assertTrue(handler.isPurchasing.first())
        assertFalse(handler.isPurchaseLoading.first())
    }

    @Test
    fun `abortPurchaseFlow resets state`() = runTest {
        handler.startPurchaseFlow()
        handler.abortPurchaseFlow()

        assertFalse(handler.isPurchasing.first())
        assertFalse(handler.isPurchaseLoading.first())
    }

    @Test
    fun `confirmPurchase resets state and returns success`() = runTest {
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.success(Balance(coins = 100, diamonds = 0)))

        val result = handler.confirmPurchase("sku-1")

        assertTrue(result.isSuccess)
        assertFalse(handler.isPurchasing.first())
        assertFalse(handler.isPurchaseLoading.first())
    }

    @Test
    fun `confirmPurchase resets state and returns failure`() = runTest {
        val error = BuyStoryError.NotPurchasable()
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.failure(error))

        val result = handler.confirmPurchase("sku-1")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        assertFalse(handler.isPurchasing.first())
        assertFalse(handler.isPurchaseLoading.first())
    }
}
