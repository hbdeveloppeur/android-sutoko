package com.purpletear.platform.play.billing

import android.os.Build
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import fr.sutoko.inapppurchase.billing.PurchaseResult
import fr.sutoko.inapppurchase.billing.VerificationResult
import fr.sutoko.inapppurchase.billing.exception.BillingException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayBillingPurchaseRecoveryTest : PlayBillingDataSourceTestFixture() {

    @Test
    fun `purchase item already owned reconciles and returns owned result`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult =
                queryResult(details = listOf(inAppProductDetails("removeads")))
            wrapper.launchResult = BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED)
                .build()
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.INAPP] =
                okResult() to listOf(
                    purchase(listOf("removeads"), "token-owned", acknowledged = true)
                )
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.SUBS] =
                okResult() to emptyList()

            val result = dataSource.purchase("removeads")

            assertTrue(result is PurchaseResult.Purchased)
            assertEquals("removeads", (result as PurchaseResult.Purchased).receipt.sku)
            assertEquals(
                1,
                wrapper.queryPurchasesCalls.count {
                    it.params.productTypeValue == BillingClient.ProductType.INAPP
                },
            )
            assertEquals(
                1,
                wrapper.queryPurchasesCalls.count {
                    it.params.productTypeValue == BillingClient.ProductType.SUBS
                },
            )
        }

    @Test
    fun `purchaseUpdates emits redelivered purchase without pending flow`() =
        runTest(dispatcher) {
            // Lazy billing client: initialize the wrapper so the purchases listener is wired.
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.INAPP] =
                okResult() to emptyList()
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.SUBS] =
                okResult() to emptyList()
            dataSource.reconcilePurchases()
            advanceUntilIdle()

            val emitted = mutableListOf<List<PurchaseResult>>()
            val received = CompletableDeferred<Unit>()
            backgroundScope.launch {
                dataSource.purchaseUpdates.collect {
                    emitted.add(it)
                    received.complete(Unit)
                }
            }
            advanceUntilIdle()

            wrapper.purchasesUpdatedListener?.onPurchasesUpdated(
                okResult(),
                listOf(purchase(listOf("gems"), "token-gems")),
            )
            advanceUntilIdle()

            withTimeout(5000) { received.await() }

            assertEquals(1, emitted.size)
            assertEquals(1, emitted.first().size)
            assertTrue(emitted.first().single() is PurchaseResult.Purchased)
        }

    @Test
    fun `reconcilePurchases queries both types does not verify and fulfills correctly`() =
        runTest(dispatcher) {
            verifier.result = VerificationResult(verified = false)

            wrapper.queryPurchasesAnswers[BillingClient.ProductType.INAPP] = okResult() to listOf(
                purchase(listOf("gems"), "token-gems", acknowledged = false)
            )
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.SUBS] = okResult() to listOf(
                purchase(listOf("premium"), "token-premium", acknowledged = false)
            )

            val results = dataSource.reconcilePurchases()

            assertEquals(2, results.size)
            assertTrue(results.all { it is PurchaseResult.Purchased })
            assertEquals(1, wrapper.consumeCalls.size)
            assertEquals("token-gems", wrapper.consumeCalls.first().params.purchaseToken)
            assertEquals(1, wrapper.acknowledgeCalls.size)
            assertEquals("token-premium", wrapper.acknowledgeCalls.first().params.purchaseToken)
        }

    @Test
    fun `queryPurchases maps google purchases to receipts via catalog`() =
        runTest(dispatcher) {
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.INAPP] = okResult() to listOf(
                purchase(listOf("gems"), "token-gems", acknowledged = true)
            )
            wrapper.queryPurchasesAnswers[BillingClient.ProductType.SUBS] =
                okResult() to emptyList()

            val receipts = dataSource.queryPurchases()

            assertEquals(1, receipts.size)
            assertEquals("gems", receipts[0].sku)
            assertEquals("token-gems", receipts[0].purchaseToken)
            assertTrue(receipts[0].acknowledged)
        }

    @Test
    fun `purchase with unknown sku returns Failed instead of throwing`() =
        runTest(dispatcher) {
            val result = dataSource.purchase("unknown")

            assertTrue(result is PurchaseResult.Failed)
            assertEquals("unknown", (result as PurchaseResult.Failed).sku)
        }

    @Test
    fun `purchase propagates CancellationException instead of returning Failed`() =
        runTest(dispatcher) {
            catalog.getProductError = CancellationException("cancelled")

            var thrown: Throwable? = null
            try {
                dataSource.purchase("gems")
            } catch (t: Throwable) {
                thrown = t
            }

            assertTrue(
                "CancellationException must escape, not be boxed into PurchaseResult.Failed",
                thrown is CancellationException,
            )
        }

    @Test
    fun `acknowledgePurchase error throws BillingException`() = runTest(dispatcher) {
        wrapper.acknowledgeResult = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.ERROR)
            .setDebugMessage("boom")
            .build()

        try {
            dataSource.acknowledgePurchase("token")
            fail("expected BillingException")
        } catch (e: BillingException) {
            assertEquals(BillingClient.BillingResponseCode.ERROR, e.responseCode)
        }
    }

    @Test
    fun `consumePurchase error throws BillingException`() = runTest(dispatcher) {
        wrapper.consumeResult = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.ERROR)
            .setDebugMessage("boom")
            .build()

        try {
            dataSource.consumePurchase("token")
            fail("expected BillingException")
        } catch (e: BillingException) {
            assertEquals(BillingClient.BillingResponseCode.ERROR, e.responseCode)
        }
    }
}
