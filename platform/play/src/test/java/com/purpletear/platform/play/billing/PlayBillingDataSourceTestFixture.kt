package com.purpletear.platform.play.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsResult
import fr.sutoko.inapppurchase.billing.BillingProduct
import fr.sutoko.inapppurchase.billing.PlayBillingDataSource
import fr.sutoko.inapppurchase.billing.ProductKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.robolectric.Robolectric

/**
 * Shared fixture for [PlayBillingDataSource] tests: fresh Robolectric activity,
 * fakes and data source per test class instance, plus Main dispatcher setup/cleanup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class PlayBillingDataSourceTestFixture {

    private val mainDispatcher = UnconfinedTestDispatcher()
    protected val dispatcher = StandardTestDispatcher()

    protected val wrapper = FakeBillingClientWrapper()
    private val wrapperFactory = FakeBillingClientWrapperFactory(wrapper)
    protected val activityProvider = FakeActivityProvider()
    protected val catalog = FakeBillingCatalog()
    protected val verifier = FakePurchaseVerifier()

    protected lateinit var dataSource: PlayBillingDataSource

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

    protected fun okResult(): BillingResult =
        BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.OK)
            .build()
}

internal fun queryResult(
    details: List<ProductDetails>,
    result: BillingResult = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build(),
): Pair<BillingResult, QueryProductDetailsResult> =
    result to QueryProductDetailsResult.create(details, emptyList())
