package com.purpletear.purchase.data

import com.purpletear.purchase.data.PurchaseTestFixtures.entity
import com.purpletear.purchase.data.PurchaseTestFixtures.receipt
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseRepositorySyncTest {

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
    fun `syncPurchases preserves backendRegistered when purchaseToken is unchanged`() = runTest {
        fakeDao.upsert(entity(sku = "sku", purchaseToken = "token-1", backendRegistered = true))
        fakeBilling.queryPurchasesResult = listOf(receipt(sku = "sku", purchaseToken = "token-1"))

        val result = repository.syncPurchases()

        assertTrue(result.isSuccess)
        val replaced = fakeDao.replaceAllCalls.single()
        assertTrue(replaced.single().backendRegistered)
    }

    @Test
    fun `syncPurchases resets backendRegistered when purchaseToken changes`() = runTest {
        fakeDao.upsert(entity(sku = "sku", purchaseToken = "token-1", backendRegistered = true))
        fakeBilling.queryPurchasesResult = listOf(receipt(sku = "sku", purchaseToken = "token-2"))

        val result = repository.syncPurchases()

        assertTrue(result.isSuccess)
        assertFalse(fakeDao.replaceAllCalls.single().single().backendRegistered)
    }

    @Test
    fun `syncPurchases replaces local table with billing receipts and removes stale SKUs`() =
        runTest {
            fakeDao.upsert(entity(sku = "old-sku", purchaseToken = "token-old"))
            fakeBilling.queryPurchasesResult =
                listOf(receipt(sku = "new-sku", purchaseToken = "token-new"))

            val result = repository.syncPurchases()

            assertTrue(result.isSuccess)
            assertEquals(listOf("new-sku"), fakeDao.purchases.map { it.sku })
            val replaced = fakeDao.replaceAllCalls.single()
            assertEquals("new-sku", replaced.single().sku)
        }

    @Test
    fun `syncPurchases returns failure and does not modify DAO on billing error`() = runTest {
        fakeBilling.throwOnReconcilePurchases = RuntimeException("sync failed")

        val result = repository.syncPurchases()

        assertTrue(result.isFailure)
        assertTrue(fakeDao.replaceAllCalls.isEmpty())
    }

    @Test
    fun `syncPurchases calls reconcile and query exactly once each in order`() = runTest {
        fakeBilling.reconcilePurchasesResult = emptyList()
        fakeBilling.queryPurchasesResult = listOf(receipt())

        repository.syncPurchases()

        assertEquals(1, fakeBilling.reconcilePurchasesCallCount)
        assertEquals(1, fakeBilling.queryPurchasesCallCount)
        assertEquals(1, fakeDao.replaceAllCalls.size)
    }
}
