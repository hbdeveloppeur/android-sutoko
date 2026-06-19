package com.purpletear.purchase.data

import com.purpletear.purchase.data.PurchaseTestFixtures.entity
import com.purpletear.purchase.data.PurchaseTestFixtures.productDetails
import com.purpletear.purchase.data.PurchaseTestFixtures.receipt
import fr.sutoko.inapppurchase.application.data.PurchaseRepositoryImpl
import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.billing.PurchaseReceipt
import fr.sutoko.inapppurchase.billing.PurchaseResult
import fr.sutoko.inapppurchase.billing.exception.PurchaseAlreadyOwnedException
import fr.sutoko.inapppurchase.billing.exception.PurchaseCancelledException
import fr.sutoko.inapppurchase.billing.exception.PurchaseFailedException
import fr.sutoko.inapppurchase.billing.exception.PurchasePendingException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseRepositoryImplTest {

    private val fakeDao = FakePurchaseDao()
    private val fakeBilling = FakeBillingDataSource()
    private val repository = PurchaseRepositoryImpl(fakeDao, fakeBilling)

    //region purchase()

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

    //endregion

    //region queryProductDetails()

    @Test
    fun `queryProductDetails maps billing details to domain Product`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(
            productDetails(
                sku = "sku",
                title = "Title",
                description = "Desc",
                formattedPrice = "$2.00"
            )
        )

        val result = repository.queryProductDetails("sku")

        assertTrue(result.isSuccess)
        assertEquals(
            Product(sku = "sku", title = "Title", description = "Desc", formattedPrice = "$2.00"),
            result.getOrNull()
        )
    }

    @Test
    fun `queryProductDetails returns failure when billing returns empty list`() = runTest {
        fakeBilling.queryProductDetailsListResult = emptyList()

        val result = repository.queryProductDetails("sku")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as IllegalArgumentException
        assertTrue(exception.message!!.contains("sku"))
    }

    @Test
    fun `queryProductDetails wraps billing exceptions in Result failure`() = runTest {
        val cause = RuntimeException("billing error")
        fakeBilling.throwOnQueryProductDetails = cause

        val result = repository.queryProductDetails("sku")

        assertTrue(result.isFailure)
        assertEquals(cause, result.exceptionOrNull())
    }

    @Test
    fun `queryProductDetails rejects blank SKU without calling billing`() = runTest {
        val result = repository.queryProductDetails("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(fakeBilling.queryProductDetailsCalls.isEmpty())
    }

    @Test
    fun `queryProductDetails calls billing data source exactly once`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(productDetails())

        repository.queryProductDetails("sku")

        assertEquals(1, fakeBilling.queryProductDetailsCalls.size)
    }

    @Test
    fun `queryProductDetails with multiple skus returns all products`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(
            productDetails(sku = "sku-1", formattedPrice = "$1.00"),
            productDetails(sku = "sku-2", formattedPrice = "$2.00"),
        )

        val result = repository.queryProductDetails(listOf("sku-1", "sku-2"))

        assertTrue(result.isSuccess)
        val products = result.getOrNull()!!
        assertEquals(2, products.size)
        assertEquals("$1.00", products.first { it.sku == "sku-1" }.formattedPrice)
        assertEquals("$2.00", products.first { it.sku == "sku-2" }.formattedPrice)
        assertEquals(1, fakeBilling.queryProductDetailsCalls.size)
    }

    @Test
    fun `queryProductDetails with multiple skus returns failure when one is missing`() = runTest {
        fakeBilling.queryProductDetailsListResult = listOf(productDetails(sku = "sku-1"))

        val result = repository.queryProductDetails(listOf("sku-1", "sku-2"))

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as IllegalArgumentException
        assertTrue(exception.message!!.contains("sku-2"))
    }

    @Test
    fun `queryProductDetails with empty list returns success with empty list`() = runTest {
        val result = repository.queryProductDetails(emptyList())

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
        assertTrue(fakeBilling.queryProductDetailsCalls.isEmpty())
    }

    //endregion

    //region syncPurchases()

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

    //endregion

    //region observe*() mapping

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

    //endregion

    //region entitlement flows

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

    //endregion

    //region streams

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

    //endregion
}
