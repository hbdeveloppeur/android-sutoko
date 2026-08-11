package com.purpletear.platform.play.billing

import android.os.Build
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.QueryProductDetailsResult
import fr.sutoko.inapppurchase.billing.exception.BillingException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayBillingProductQueryTest : PlayBillingDataSourceTestFixture() {

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
}
