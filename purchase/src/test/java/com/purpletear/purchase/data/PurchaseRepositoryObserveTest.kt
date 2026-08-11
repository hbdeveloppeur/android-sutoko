package com.purpletear.purchase.data

import com.purpletear.purchase.data.PurchaseTestFixtures.entity
import com.purpletear.purchase.data.PurchaseTestFixtures.receipt
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.billing.PurchaseResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseRepositoryObserveTest {

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
    fun `observePurchases maps DAO entities to domain purchases preserving order`() = runTest {
        fakeDao.upsert(entity(sku = "a", purchaseToken = "t1"))
        fakeDao.upsert(entity(sku = "b", purchaseToken = "t2"))

        val purchases = repository.observePurchases().first()

        assertEquals(listOf("a", "b"), purchases.map { it.sku })
        assertEquals("t1", purchases[0].purchaseToken)
        assertEquals("t2", purchases[1].purchaseToken)
    }

    @Test
    fun `observePurchase maps entity to domain and null to null`() = runTest {
        fakeDao.upsert(entity(sku = "present"))

        val found = repository.observePurchase("present").first()
        assertEquals("present", found?.sku)

        val missing = repository.observePurchase("absent").first()
        assertNull(missing)
    }

    @Test
    fun `observeUnregisteredPurchases filters by PURCHASED state and maps results`() = runTest {
        fakeDao.upsert(entity(sku = "registered", backendRegistered = true))
        fakeDao.upsert(entity(sku = "unregistered", backendRegistered = false))
        fakeDao.upsert(
            entity(
                sku = "pending",
                purchaseState = PurchaseState.PENDING,
                backendRegistered = false
            )
        )

        val unregistered = repository.observeUnregisteredPurchases().first()

        assertEquals(listOf("unregistered"), unregistered.map { it.sku })
    }

    @Test
    fun `markBackendRegistered delegates exact SKU to DAO`() = runTest {
        repository.markBackendRegistered("sku")

        assertEquals(listOf("sku"), fakeDao.markedBackendRegisteredSkus)
    }

    @Test
    fun `deletePurchase removes the local row and observePurchase emits without it`() = runTest {
        fakeDao.upsert(entity(sku = "sku"))
        assertEquals(listOf("sku"), repository.observePurchases().first().map { it.sku })

        repository.deletePurchase("sku")

        assertEquals(listOf("sku"), fakeDao.deletedSkus)
        assertTrue(repository.observePurchases().first().isEmpty())
        assertNull(repository.observePurchase("sku").first())
    }

    @Test
    fun `purchaseUpdates emits Unit only when update contains a Purchased result`() = runTest {
        val emissions = mutableListOf<Unit>()
        val job = backgroundScope.launch {
            repository.purchaseUpdates.collect { emissions += it }
        }
        runCurrent()

        fakeBilling.purchaseUpdatesFlow.emit(listOf(PurchaseResult.Canceled))
        runCurrent()
        fakeBilling.purchaseUpdatesFlow.emit(listOf(PurchaseResult.AlreadyOwned("sku")))
        runCurrent()
        fakeBilling.purchaseUpdatesFlow.emit(
            listOf(
                PurchaseResult.Pending(receipt()),
                PurchaseResult.Purchased(receipt(sku = "sku"))
            )
        )
        runCurrent()

        job.cancel()

        assertEquals(listOf(Unit), emissions)
    }

    @Test
    fun `connectionState passes through billing connection state unchanged`() = runTest {
        val emissions = mutableListOf<Boolean>()
        val job = backgroundScope.launch {
            repository.connectionState.collect { emissions += it }
        }
        runCurrent()

        fakeBilling.connectionStateFlow.emit(false)
        runCurrent()
        fakeBilling.connectionStateFlow.emit(true)
        runCurrent()

        job.cancel()

        assertEquals(listOf(false, true), emissions)
    }

    @Test
    fun `public flows cancel upstream Room subscription when collector cancels`() = runTest {
        val job = backgroundScope.launch {
            repository.observePurchases().collect { }
        }
        runCurrent()
        assertEquals(1, fakeDao.observeAllSubscriptionCount.get())

        job.cancel()
        runCurrent()
        assertEquals(0, fakeDao.observeAllSubscriptionCount.get())
    }
}
