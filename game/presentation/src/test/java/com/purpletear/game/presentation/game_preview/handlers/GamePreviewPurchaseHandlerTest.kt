package com.purpletear.game.presentation.game_preview.handlers

import com.purpletear.game.presentation.game_preview.fakes.FakePurchaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePreviewPurchaseHandlerTest {

    private val purchaseRepository = FakePurchaseRepository()
    private val handler = GamePreviewPurchaseHandler(purchaseRepository)

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
        purchaseRepository.setPurchaseResult("sku-1", Result.success(Unit))

        val result = handler.confirmPurchase("sku-1")

        assertTrue(result.isSuccess)
        assertFalse(handler.isPurchasing.first())
        assertFalse(handler.isPurchaseLoading.first())
    }

    @Test
    fun `confirmPurchase resets state and returns failure`() = runTest {
        val error = RuntimeException("purchase failed")
        purchaseRepository.setPurchaseResult("sku-1", Result.failure(error))

        val result = handler.confirmPurchase("sku-1")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        assertFalse(handler.isPurchasing.first())
        assertFalse(handler.isPurchaseLoading.first())
    }
}
