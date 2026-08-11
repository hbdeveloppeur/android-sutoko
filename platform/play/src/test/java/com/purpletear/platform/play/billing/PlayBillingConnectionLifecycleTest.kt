package com.purpletear.platform.play.billing

import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
internal class PlayBillingConnectionLifecycleTest : PlayBillingDataSourceTestFixture() {

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
}
