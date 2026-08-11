package com.purpletear.platform.play.billing

import android.os.Build
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import fr.sutoko.inapppurchase.billing.BillingProduct
import fr.sutoko.inapppurchase.billing.ProductKind
import fr.sutoko.inapppurchase.billing.PurchaseResult
import fr.sutoko.inapppurchase.billing.VerificationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayBillingPurchaseFlowTest : PlayBillingDataSourceTestFixture() {

    @Test
    fun `purchase activity unavailable before launch returns Failed without throwing`() =
        runTest(dispatcher) {
            activityProvider.currentActivity = null

            val result = dataSource.purchase("gems")

            assertTrue(result is PurchaseResult.Failed)
            assertEquals("gems", (result as PurchaseResult.Failed).sku)
            assertTrue(wrapper.launchCalls.isEmpty())
        }

    @Test
    fun `purchase second flow while pending returns already in progress Failed`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult = queryResult(
                details = listOf(
                    inAppProductDetails("gems"),
                    inAppProductDetails("removeads"),
                )
            )

            val first = async { dataSource.purchase("gems") }
            advanceUntilIdle()

            val second = dataSource.purchase("removeads")

            assertTrue(second is PurchaseResult.Failed)
            assertEquals("removeads", second.sku)
            assertTrue((second as PurchaseResult.Failed).message.contains("already in progress"))

            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                okResult(),
                listOf(purchase(listOf("gems"), "token-gems")),
            )
            first.await()
        }

    @Test
    fun `purchase listener delivers non matching sku then matching sku completes only on match`() =
        runTest(dispatcher) {
            catalog.add(BillingProduct("sku_a", ProductKind.CONSUMABLE))
            catalog.add(BillingProduct("sku_b", ProductKind.CONSUMABLE))
            wrapper.queryProductDetailsResult =
                queryResult(details = listOf(inAppProductDetails("sku_a")))

            val deferredResult = async { dataSource.purchase("sku_a") }

            advanceUntilIdle()

            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                okResult(),
                listOf(purchase(listOf("sku_b"), "token-b")),
            )

            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                okResult(),
                listOf(purchase(listOf("sku_a"), "token-a")),
            )

            val result = deferredResult.await()
            assertTrue(result is PurchaseResult.Purchased)
            assertEquals("sku_a", (result as PurchaseResult.Purchased).receipt.sku)
        }

    @Test
    fun `purchase user canceled returns Canceled and clears pending`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult =
                queryResult(details = listOf(inAppProductDetails("gems")))

            val deferredResult = async { dataSource.purchase("gems") }

            advanceUntilIdle()
            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.USER_CANCELED)
                    .build(),
                emptyList(),
            )

            assertEquals(PurchaseResult.Canceled, deferredResult.await())
        }

    @Test
    fun `purchase play error after launch returns Failed and clears pending`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult =
                queryResult(details = listOf(inAppProductDetails("gems")))

            val deferredResult = async { dataSource.purchase("gems") }

            advanceUntilIdle()
            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
                    .setDebugMessage("item unavailable")
                    .build(),
                emptyList(),
            )

            val result = deferredResult.await()
            assertTrue(result is PurchaseResult.Failed)
            assertEquals(
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                (result as PurchaseResult.Failed).responseCode,
            )
        }

    @Test
    fun `purchase verification failure prevents consume or acknowledge`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult =
                queryResult(details = listOf(inAppProductDetails("gems")))
            verifier.result = VerificationResult(verified = false, message = "bad sig")

            val deferredResult = async { dataSource.purchase("gems") }

            advanceUntilIdle()
            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                okResult(),
                listOf(purchase(listOf("gems"), "token-gems")),
            )

            val result = deferredResult.await()
            assertTrue(result is PurchaseResult.Failed)
            assertEquals("bad sig", (result as PurchaseResult.Failed).message)
            assertTrue(wrapper.consumeCalls.isEmpty())
            assertTrue(wrapper.acknowledgeCalls.isEmpty())
        }

    @Test
    fun `purchase success consumes consumable`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult =
            queryResult(details = listOf(inAppProductDetails("gems")))

        val deferredResult = async { dataSource.purchase("gems") }

        advanceUntilIdle()
        wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
            okResult(),
            listOf(purchase(listOf("gems"), "token-gems", acknowledged = false)),
        )

        val result = deferredResult.await()
        assertTrue(result is PurchaseResult.Purchased)
        assertEquals("gems", (result as PurchaseResult.Purchased).receipt.sku)
        assertFalse(result.receipt.acknowledged)

        assertEquals(1, wrapper.consumeCalls.size)
        assertEquals("token-gems", wrapper.consumeCalls.first().params.purchaseToken)
        assertTrue(wrapper.acknowledgeCalls.isEmpty())
    }

    @Test
    fun `purchase success acknowledges non consumable`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult =
            queryResult(details = listOf(inAppProductDetails("removeads")))

        val deferredResult = async { dataSource.purchase("removeads") }

        advanceUntilIdle()
        wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
            okResult(),
            listOf(purchase(listOf("removeads"), "token-ads", acknowledged = false)),
        )

        val result = deferredResult.await()
        assertTrue(result is PurchaseResult.Purchased)
        assertTrue((result as PurchaseResult.Purchased).receipt.acknowledged)

        assertEquals(1, wrapper.acknowledgeCalls.size)
        assertEquals("token-ads", wrapper.acknowledgeCalls.first().params.purchaseToken)
        assertTrue(wrapper.consumeCalls.isEmpty())
    }

    @Test
    fun `purchase success acknowledges subscription`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(
            details = listOf(
                subscriptionProductDetails("premium", offerToken = "offer-premium")
            )
        )

        val deferredResult = async { dataSource.purchase("premium") }

        advanceUntilIdle()
        wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
            okResult(),
            listOf(purchase(listOf("premium"), "token-premium", acknowledged = false)),
        )

        val result = deferredResult.await()
        assertTrue(result is PurchaseResult.Purchased)
        assertTrue((result as PurchaseResult.Purchased).receipt.acknowledged)

        assertEquals(1, wrapper.acknowledgeCalls.size)
        assertEquals("token-premium", wrapper.acknowledgeCalls.first().params.purchaseToken)
        assertTrue(wrapper.consumeCalls.isEmpty())
    }

    @Test
    fun `purchase pending state returns Pending without fulfillment`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult =
            queryResult(details = listOf(inAppProductDetails("gems")))

        val deferredResult = async { dataSource.purchase("gems") }

        advanceUntilIdle()
        wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
            okResult(),
            listOf(
                purchase(
                    listOf("gems"),
                    "token-gems",
                    purchaseState = Purchase.PurchaseState.PENDING,
                )
            ),
        )

        val result = deferredResult.await()
        assertTrue(result is PurchaseResult.Pending)
        assertTrue(wrapper.consumeCalls.isEmpty())
        assertTrue(wrapper.acknowledgeCalls.isEmpty())
    }
}
