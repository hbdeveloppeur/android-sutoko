package com.purpletear.purchase.data

import com.purpletear.purchase.data.PurchaseTestFixtures.entity
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseRepositoryEntitlementTest {

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
    fun `observePurchasedSkus returns only PURCHASED SKUs as a set`() = runTest {
        fakeDao.upsert(entity(sku = "purchased", purchaseState = PurchaseState.PURCHASED))
        fakeDao.upsert(entity(sku = "pending", purchaseState = PurchaseState.PENDING))

        val skus = repository.observePurchasedSkus().first()

        assertEquals(setOf("purchased"), skus)
    }

    @Test
    fun `observeHasGlobalPremium is true when an owned SKU contains premium case-insensitively`() =
        runTest {
            fakeDao.upsert(entity(sku = "Premium_Pass"))

            assertTrue(repository.observeHasGlobalPremium().first())
        }

    @Test
    fun `observeHasGlobalPremium is false for non-premium SKUs`() = runTest {
        fakeDao.upsert(entity(sku = "gem_pack"))

        assertFalse(repository.observeHasGlobalPremium().first())
    }

    @Test
    fun `observeHasGlobalPremium is false for pending premium purchases`() = runTest {
        fakeDao.upsert(entity(sku = "premium", purchaseState = PurchaseState.PENDING))

        assertFalse(repository.observeHasGlobalPremium().first())
    }

    @Test
    fun `observeIsPurchased is true when requested SKU is owned`() = runTest {
        fakeDao.upsert(entity(sku = "target"))

        assertTrue(repository.observeIsPurchased(listOf("target")).first())
    }

    @Test
    fun `observeIsPurchased is false when only an unrelated SKU is owned`() = runTest {
        fakeDao.upsert(entity(sku = "other"))

        assertFalse(repository.observeIsPurchased(listOf("target")).first())
    }

    @Test
    fun `observeIsPurchased is true under global premium for any requested SKU`() = runTest {
        fakeDao.upsert(entity(sku = "premium_pass"))

        assertTrue(repository.observeIsPurchased(listOf("anything")).first())
    }

    @Test
    fun `observeIsPurchased is false while requested SKU is PENDING`() = runTest {
        fakeDao.upsert(entity(sku = "target", purchaseState = PurchaseState.PENDING))

        assertFalse(repository.observeIsPurchased(listOf("target")).first())
    }
}
