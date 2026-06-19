package com.purpletear.platform.play.billing

import android.app.Activity
import android.os.Build
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsResult
import fr.sutoko.inapppurchase.billing.BillingProduct
import fr.sutoko.inapppurchase.billing.PlayBillingDataSource
import fr.sutoko.inapppurchase.billing.ProductKind
import fr.sutoko.inapppurchase.billing.PurchaseResult
import fr.sutoko.inapppurchase.billing.VerificationResult
import fr.sutoko.inapppurchase.billing.exception.BillingException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayBillingDataSourceTest {

    private val mainDispatcher = UnconfinedTestDispatcher()
    private val dispatcher = StandardTestDispatcher()

    private val wrapper = FakeBillingClientWrapper()
    private val wrapperFactory = FakeBillingClientWrapperFactory(wrapper)
    private val activityProvider = FakeActivityProvider()
    private val catalog = FakeBillingCatalog()
    private val verifier = FakePurchaseVerifier()

    private lateinit var dataSource: PlayBillingDataSource

    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)

        activityProvider.currentActivity =
            Robolectric.buildActivity(Activity::class.java).create().get()

        catalog.add(BillingProduct("gems", ProductKind.CONSUMABLE))
        catalog.add(BillingProduct("removeads", ProductKind.NON_CONSUMABLE))
        catalog.add(BillingProduct("premium", ProductKind.SUBSCRIPTION))

        dataSource = PlayBillingDataSource(
            wrapperFactory = wrapperFactory,
            activityProvider = activityProvider,
            verifier = verifier,
            catalog = catalog,
            ioDispatcher = dispatcher,
        )
    }

    @After
    fun tearDown() {
        dataSource.close()
        Dispatchers.resetMain()
    }

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
    fun `queryProductDetails error callback after cancellation does not crash`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult = null
            wrapper.connectionResult = okResult()

            val job = launch { dataSource.queryProductDetails("gems") }
            advanceUntilIdle()
            job.cancel()

            val callback = wrapper.queryProductDetailsCalls.last().callback
            callback.onProductDetailsResponse(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                    .setDebugMessage("boom")
                    .build(),
                QueryProductDetailsResult.create(emptyList(), emptyList()),
            )

            assertTrue(job.isCancelled)
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
    fun `queryProductDetails returns mapped domain details`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(
            details = listOf(
                inAppProductDetails(
                    sku = "gems",
                    title = "Gems",
                    description = "100 gems",
                    formattedPrice = "$2.00",
                )
            )
        )

        val details = dataSource.queryProductDetails("gems")

        assertNotNull(details)
        assertEquals("gems", details!!.sku)
        assertEquals("Gems", details.title)
        assertEquals("100 gems", details.description)
        assertEquals("$2.00", details.formattedPrice)
    }

    @Test
    fun `queryProductDetails empty result returns null`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(details = emptyList())

        val details = dataSource.queryProductDetails("gems")

        assertNull(details)
    }

    @Test
    fun `queryProductDetails with multiple skus returns mapped details`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(
            details = listOf(
                inAppProductDetails(
                    sku = "gems",
                    title = "Gems",
                    description = "100 gems",
                    formattedPrice = "$2.00",
                ),
                inAppProductDetails(
                    sku = "removeads",
                    title = "Remove Ads",
                    description = "No ads forever",
                    formattedPrice = "$5.00",
                ),
            )
        )

        val details = dataSource.queryProductDetails(listOf("gems", "removeads"))

        assertEquals(2, details.size)
        assertEquals("gems", details[0].sku)
        assertEquals("Gems", details[0].title)
        assertEquals("$2.00", details[0].formattedPrice)
        assertEquals("removeads", details[1].sku)
        assertEquals("Remove Ads", details[1].title)
        assertEquals("$5.00", details[1].formattedPrice)
    }

    @Test
    fun `queryProductDetails with multiple skus makes a single billing call`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(
            details = listOf(
                inAppProductDetails("gems"),
                inAppProductDetails("removeads"),
            )
        )

        dataSource.queryProductDetails(listOf("gems", "removeads"))

        assertEquals(1, wrapper.queryProductDetailsCalls.size)
    }

    @Test
    fun `queryProductDetails with multiple skus returns only found products`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(
            details = listOf(inAppProductDetails("gems"))
        )

        val details = dataSource.queryProductDetails(listOf("gems", "removeads"))

        assertEquals(1, details.size)
        assertEquals("gems", details.single().sku)
    }

    @Test
    fun `queryProductDetails error response throws BillingException`() = runTest(dispatcher) {
        wrapper.queryProductDetailsResult = queryResult(
            details = emptyList(),
            result = BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .setDebugMessage("boom")
                .build()
        )

        try {
            dataSource.queryProductDetails(listOf("gems"))
            fail("expected BillingException")
        } catch (e: BillingException) {
            assertEquals(BillingClient.BillingResponseCode.ERROR, e.responseCode)
        }
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

    @Test
    fun `connectionState emits true after successful connection`() = runTest(dispatcher) {
        assertFalse(dataSource.connectionState.value)

        wrapper.queryProductDetailsResult = queryResult(details = emptyList())
        dataSource.queryProductDetails("gems")

        assertTrue(dataSource.connectionState.value)
    }

    @Test
    fun `close cancels pending purchase ends connection sets state false`() =
        runTest(dispatcher) {
            wrapper.queryProductDetailsResult =
                queryResult(details = listOf(inAppProductDetails("gems")))

            val job = launch { dataSource.purchase("gems") }
            advanceUntilIdle()

            dataSource.close()
            advanceUntilIdle()

            assertTrue(job.isCancelled)
            assertEquals(1, wrapper.endConnectionCalled)
            assertFalse(dataSource.connectionState.value)
        }

    @Test
    fun `purchase with unknown sku returns Failed instead of throwing`() =
        runTest(dispatcher) {
            val result = dataSource.purchase("unknown")

            assertTrue(result is PurchaseResult.Failed)
            assertEquals("unknown", (result as PurchaseResult.Failed).sku)
        }

    private fun okResult(): BillingResult =
        BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.OK)
            .build()
}

private fun queryResult(
    details: List<ProductDetails>,
    result: BillingResult = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build(),
): Pair<BillingResult, QueryProductDetailsResult> =
    result to QueryProductDetailsResult.create(details, emptyList())
