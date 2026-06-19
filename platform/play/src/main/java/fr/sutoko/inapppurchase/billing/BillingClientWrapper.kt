package fr.sutoko.inapppurchase.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface BillingClientWrapper {
    val isReady: Boolean
    fun startConnection(listener: BillingClientStateListener)
    fun endConnection()
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult
    fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        callback: ProductDetailsResponseListener
    )

    fun queryPurchasesAsync(params: QueryPurchasesParams, callback: PurchasesResponseListener)
    fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        callback: AcknowledgePurchaseResponseListener
    )

    fun consumeAsync(params: ConsumeParams, callback: ConsumeResponseListener)
}

fun interface BillingClientWrapperFactory {
    fun create(listener: PurchasesUpdatedListener): BillingClientWrapper
}

class PlayBillingClientWrapperFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : BillingClientWrapperFactory {
    override fun create(listener: PurchasesUpdatedListener): BillingClientWrapper {
        return PlayBillingClientWrapper(context, listener)
    }
}

class PlayBillingClientWrapper(
    context: Context,
    listener: PurchasesUpdatedListener,
) : BillingClientWrapper {

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(listener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    override val isReady: Boolean get() = client.isReady

    override fun startConnection(listener: BillingClientStateListener) {
        client.startConnection(listener)
    }

    override fun endConnection() {
        client.endConnection()
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        return client.launchBillingFlow(activity, params)
    }

    override fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        callback: ProductDetailsResponseListener
    ) {
        client.queryProductDetailsAsync(params, callback)
    }

    override fun queryPurchasesAsync(
        params: QueryPurchasesParams,
        callback: PurchasesResponseListener
    ) {
        client.queryPurchasesAsync(params, callback)
    }

    override fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        callback: AcknowledgePurchaseResponseListener
    ) {
        client.acknowledgePurchase(params, callback)
    }

    override fun consumeAsync(params: ConsumeParams, callback: ConsumeResponseListener) {
        client.consumeAsync(params, callback)
    }
}
