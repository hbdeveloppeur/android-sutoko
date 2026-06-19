package com.purpletear.platform.play.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import fr.sutoko.inapppurchase.billing.BillingClientWrapper

internal val QueryPurchasesParams.productTypeValue: String
    get() = QueryPurchasesParams::class.java
        .getDeclaredField("zza")
        .apply { isAccessible = true }
        .get(this) as String

internal class FakeBillingClientWrapper : BillingClientWrapper {

    override var isReady: Boolean = false

    var connectionResult: BillingResult? = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build()

    var purchasesUpdatedListener: PurchasesUpdatedListener? = null
    private var stateListener: BillingClientStateListener? = null

    var launchResult: BillingResult = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build()

    var queryProductDetailsResult: Pair<BillingResult, QueryProductDetailsResult>? = null
    var queryPurchasesAnswers = mutableMapOf<String, Pair<BillingResult, List<Purchase>>>()
    var acknowledgeResult: BillingResult? = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build()
    var consumeResult: BillingResult? = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build()

    private var endConnectionCount = 0
    val endConnectionCalled: Int get() = endConnectionCount

    data class LaunchCall(val activity: Activity, val params: BillingFlowParams)

    val launchCalls = mutableListOf<LaunchCall>()

    data class QueryProductDetailsCall(
        val params: QueryProductDetailsParams,
        val callback: ProductDetailsResponseListener,
    )

    val queryProductDetailsCalls = mutableListOf<QueryProductDetailsCall>()

    data class QueryPurchasesCall(
        val params: QueryPurchasesParams,
        val callback: PurchasesResponseListener,
    )

    val queryPurchasesCalls = mutableListOf<QueryPurchasesCall>()

    data class AcknowledgeCall(
        val params: AcknowledgePurchaseParams,
        val callback: AcknowledgePurchaseResponseListener,
    )

    val acknowledgeCalls = mutableListOf<AcknowledgeCall>()

    data class ConsumeCall(val params: ConsumeParams, val callback: ConsumeResponseListener)

    val consumeCalls = mutableListOf<ConsumeCall>()

    override fun startConnection(listener: BillingClientStateListener) {
        stateListener = listener
        connectionResult?.let { listener.onBillingSetupFinished(it) }
    }

    override fun endConnection() {
        endConnectionCount++
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        launchCalls.add(LaunchCall(activity, params))
        return launchResult
    }

    override fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        callback: ProductDetailsResponseListener,
    ) {
        queryProductDetailsCalls.add(QueryProductDetailsCall(params, callback))
        queryProductDetailsResult?.let { (result, queryResult) ->
            callback.onProductDetailsResponse(result, queryResult)
        }
    }

    override fun queryPurchasesAsync(
        params: QueryPurchasesParams,
        callback: PurchasesResponseListener,
    ) {
        queryPurchasesCalls.add(QueryPurchasesCall(params, callback))
        queryPurchasesAnswers[params.productTypeValue]?.let { (result, list) ->
            callback.onQueryPurchasesResponse(result, list)
        }
    }

    override fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        callback: AcknowledgePurchaseResponseListener,
    ) {
        acknowledgeCalls.add(AcknowledgeCall(params, callback))
        acknowledgeResult?.let { callback.onAcknowledgePurchaseResponse(it) }
    }

    override fun consumeAsync(params: ConsumeParams, callback: ConsumeResponseListener) {
        consumeCalls.add(ConsumeCall(params, callback))
        consumeResult?.let { callback.onConsumeResponse(it, "") }
    }

    fun answerProductDetails(result: BillingResult, list: List<ProductDetails>) {
        queryProductDetailsCalls.lastOrNull()?.callback?.onProductDetailsResponse(
            result,
            QueryProductDetailsResult.create(list, emptyList())
        )
    }

    fun answerPurchases(productType: String, result: BillingResult, list: List<Purchase>) {
        queryPurchasesCalls.lastOrNull { it.params.productTypeValue == productType }
            ?.callback?.onQueryPurchasesResponse(result, list)
    }

    fun finishConnection(result: BillingResult) {
        stateListener?.onBillingSetupFinished(result)
    }

    fun disconnect() {
        stateListener?.onBillingServiceDisconnected()
    }
}
