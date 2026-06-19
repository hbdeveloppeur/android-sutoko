package com.purpletear.platform.play.billing

import android.os.Build
import fr.sutoko.inapppurchase.billing.PlayBillingCatalog
import fr.sutoko.inapppurchase.billing.ProductKind
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
internal class PlayBillingCatalogTest {

    private val catalog = PlayBillingCatalog()

    @Test
    fun `getProduct classifies known ids into correct kind`() = runTest {
        assertEquals(ProductKind.NON_CONSUMABLE, catalog.getProduct("removeads").kind)
        assertEquals(ProductKind.SUBSCRIPTION, catalog.getProduct("premium").kind)
        assertEquals(ProductKind.NON_CONSUMABLE, catalog.getProduct("story_pack").kind)
    }

    @Test
    fun `getProduct defaults unknown ids to consumable`() = runTest {
        assertEquals(ProductKind.CONSUMABLE, catalog.getProduct("coins").kind)
    }
}
