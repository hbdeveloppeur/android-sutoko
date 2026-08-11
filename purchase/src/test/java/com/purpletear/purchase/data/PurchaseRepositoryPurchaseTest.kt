package com.purpletear.purchase.data

import com.purpletear.purchase.data.PurchaseTestFixtures.receipt
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import fr.sutoko.inapppurchase.billing.PurchaseReceipt
import fr.sutoko.inapppurchase.billing.PurchaseResult
import fr.sutoko.inapppurchase.billing.exception.PurchaseAlreadyOwnedException
import fr.sutoko.inapppurchase.billing.exception.PurchaseCancelledException
import fr.sutoko.inapppurchase.billing.exception.PurchaseFailedException
import fr.sutoko.inapppurchase.billing.exception.PurchasePendingException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class PurchaseRepositoryPurchaseTest {

    private val fakeDao = FakePurchaseDao()
    private val fakeBilling = FakeBillingDataSource()
    private val repository =
        PurchaseRepositoryImpl(fakeDao, fakeBilling, FakeAnalyticsTracker())

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val events = mutableListOf<String>()
        override fun logEvent(name: String, params: Map<String, Any?>) {
            events += name
        }
        override fun setUserProperty(name: String, value: String?) = Unit
    }

    @Test
    fun `purchase persists Purchased receipt and returns success`() = runTest {
        val purchaseReceipt: PurchaseReceipt = receipt(sku = "sku", purchaseToken = "token-1")
        fakeBilling.purchaseResult = PurchaseResult.Purchased(purchaseReceipt)

        val result = repository.purchase("sku")

        assertTrue(result.isSuccess)
        assertEquals(listOf("sku"), fakeBilling.purchaseCalls)
        val saved = fakeDao.upsertedEntities.single()
        assertEquals(purchaseReceipt.sku, saved.sku)
        assertEquals(purchaseReceipt.purchaseToken, saved.purchaseToken)
        assertEquals(purchaseReceipt.purchaseTime, saved.purchaseTime)
        assertEquals(purchaseReceipt.acknowledged, saved.acknowledged)
        assertEquals(purchaseReceipt.purchaseState, saved.purchaseState)
        assertEquals(purchaseReceipt.orderId, saved.orderId)
        assertFalse(saved.backendRegistered)
    }

    @Test
    fun `purchase persists Pending receipt and returns PurchasePendingException`() = runTest {
        val purchaseReceipt = receipt(sku = "sku")
        fakeBilling.purchaseResult = PurchaseResult.Pending(purchaseReceipt)

        val result = repository.purchase("sku")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as PurchasePendingException
        assertEquals("sku", exception.sku)
        assertFalse(fakeDao.upsertedEntities.single().backendRegistered)
    }

    @Test
    fun `purchase Canceled returns PurchaseCancelledException and writes nothing`() = runTest {
        fakeBilling.purchaseResult = PurchaseResult.Canceled

        val result = repository.purchase("sku")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PurchaseCancelledException)
        assertTrue(fakeDao.upsertedEntities.isEmpty())
    }

    @Test
    fun `purchase Failed returns PurchaseFailedException with details and writes nothing`() =
        runTest {
            fakeBilling.purchaseResult = PurchaseResult.Failed(
                sku = "sku",
                responseCode = 7,
                message = "network error"
            )

            val result = repository.purchase("sku")

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as PurchaseFailedException
            assertEquals("sku", exception.sku)
            assertEquals(7, exception.responseCode)
            assertEquals("network error", exception.debugMessage)
            assertTrue(fakeDao.upsertedEntities.isEmpty())
        }

    @Test
    fun `purchase AlreadyOwned returns PurchaseAlreadyOwnedException and writes nothing`() =
        runTest {
            fakeBilling.purchaseResult = PurchaseResult.AlreadyOwned("sku")

            val result = repository.purchase("sku")

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as PurchaseAlreadyOwnedException
            assertEquals("sku", exception.sku)
            assertTrue(fakeDao.upsertedEntities.isEmpty())
        }

    @Test
    fun `purchase propagates CancellationException without wrapping`() = runTest {
        fakeBilling.throwOnPurchase = CancellationException("cancelled")

        var thrown: Throwable? = null
        try {
            repository.purchase("sku")
        } catch (e: Throwable) {
            thrown = e
        }

        assertTrue(thrown is CancellationException)
    }

    @Test
    fun `purchase wraps unexpected billing exceptions in Result failure`() = runTest {
        val cause = RuntimeException("boom")
        fakeBilling.throwOnPurchase = cause

        val result = repository.purchase("sku")

        assertTrue(result.isFailure)
        assertEquals(cause, result.exceptionOrNull())
        assertTrue(fakeDao.upsertedEntities.isEmpty())
    }

    @Test
    fun `purchase rejects blank SKU without calling dependencies`() = runTest {
        val result = repository.purchase("  ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(fakeBilling.purchaseCalls.isEmpty())
        assertTrue(fakeDao.upsertedEntities.isEmpty())
    }

    @Test
    fun `purchase calls billing data source exactly once`() = runTest {
        fakeBilling.purchaseResult = PurchaseResult.Purchased(receipt())

        repository.purchase("sku")

        assertEquals(1, fakeBilling.purchaseCalls.size)
    }
}
