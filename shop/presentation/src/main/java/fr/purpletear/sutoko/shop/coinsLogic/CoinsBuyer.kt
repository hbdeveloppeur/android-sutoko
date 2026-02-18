package fr.purpletear.sutoko.shop.coinsLogic

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import fr.sutoko.inapppurchase.data.utils.AppProductDetailFromGooglePlayBilling
import fr.sutoko.inapppurchase.domain.model.AppProductDetails


interface CoinsBuyerListener {
    fun onAlreadyBought()
    fun onProductBought(purchase: Purchase)
    fun onError(responseCode: Int)
    fun onAcknowledgeFailure()
}

/**
 * Handles the Premium in app purchase
 * You can :
 * - Connect,
 * - Buy,
 * - Get products info,
 * - Acknowledge the product (setDelivered),
 * - Get a list of unacknowledged purchases (getUnacknowledgedPurchases)
 * @property isConnected Boolean
 * @property billingClient BillingClient?
 * @constructor
 */
class CoinsBuyer(activity: Activity, private val listener: CoinsBuyerListener) :
    PurchasesUpdatedListener {
    var isConnected: Boolean = false
        private set

    private var activity: Activity? = activity
    var isPaused: Boolean = false

    companion object {
        private const val TAG: String = "CoinsBuyer"
    }

    private var billingClient: BillingClient? = null

    init {
        Log.d(TAG, "Initialisation")

        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    }


    /**
     * Permits to start the BillingClient connection
     * @param onConnected Function0<Unit>?
     * @param onConnectionFailed Function0<Unit>?
     */
    fun connect(onConnected: (() -> Unit)? = null, onConnectionFailed: (() -> Unit)? = null) {
        check(billingClient != null)
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isConnected = false
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                handleBillingResult(result, {
                    if (onConnected != null) {
                        onConnected()
                    }
                }, onConnectionFailed)
            }
        })
    }


    fun getProductsInfo(sku: Array<String>, onFound: (AppProductDetails) -> Unit) {

        val productList = ArrayList<QueryProductDetailsParams.Product>()
        sku.forEach {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        billingClient!!.queryProductDetailsAsync(
            params.build()
        ) { billingResult, skuDetails ->
            handleBillingResult(billingResult, {
                skuDetails.forEach {
                    Handler(Looper.getMainLooper()).post {
                        onFound(
                            AppProductDetailFromGooglePlayBilling.execute(it)
                        )
                    }
                }
            })
        }
    }

    fun getPremiumKeyInfos(sku: String, onFound: (ProductDetails) -> Unit) {

        val productList = ArrayList<QueryProductDetailsParams.Product>()

        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        billingClient!!.queryProductDetailsAsync(
            params.build()
        ) { billingResult, skuDetails ->
            handleBillingResult(billingResult, {
                skuDetails.forEach {
                    Handler(Looper.getMainLooper()).post {
                        onFound(it)
                    }
                }
            })
        }
    }

    /**
     * Tries to buy the premium subscription
     */
    fun buy(
        activity: Activity,
        sku: String,
        onConnectionFailed: () -> Unit
    ) {

        if (!isConnected) {
            Log.d(TAG, "Connect buy since the connection failed...")
            onConnectionFailed()
            return
        }


        check(billingClient != null)


        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)


        // leverage querySkuDetails Kotlin extension function
        billingClient!!.queryProductDetailsAsync(params.build()) { billingResult, details ->
            details.forEach {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(it)
                        .build()
                )
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                val responseCode =
                    billingClient!!.launchBillingFlow(activity, flowParams).responseCode

                if (responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
                    this@CoinsBuyer.listener.onAlreadyBought()
                }
            }
        }
    }


    fun setDelivered(purchase: Purchase, onComplete: (isSuccessful: Boolean) -> Unit) {
        acknowledge(purchase) /* onSuccess */ { isSuccessful ->
            when {
                isSuccessful -> {
                    consume(purchase) { result, _ ->
                        onComplete(result)
                    }
                }

                else -> {
                    onComplete(false)
                }
            }
        }
    }

    private fun consume(purchase: Purchase, onComplete: (Boolean, Int?) -> Unit) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        this.billingClient!!.consumeAsync(consumeParams, object : ConsumeResponseListener {
            override fun onConsumeResponse(result: BillingResult, str: String) {
                if (BillingResponseCode.BILLING_UNAVAILABLE == result.responseCode
                    || BillingResponseCode.FEATURE_NOT_SUPPORTED == result.responseCode
                    || BillingResponseCode.SERVICE_DISCONNECTED == result.responseCode
                    || BillingResponseCode.NETWORK_ERROR == result.responseCode
                    || BillingResponseCode.SERVICE_UNAVAILABLE == result.responseCode
                    || BillingResponseCode.ERROR == result.responseCode
                    || BillingResponseCode.DEVELOPER_ERROR == result.responseCode
                    || BillingResponseCode.ITEM_NOT_OWNED == result.responseCode
                    || BillingResponseCode.USER_CANCELED == result.responseCode
                ) {
                    Handler(Looper.getMainLooper()).post {
                        onComplete(false, result.responseCode)
                    }
                    return
                }
                Handler(Looper.getMainLooper()).post {
                    onComplete(true, null)
                }
            }

        })
    }

    fun onDestroy() {
        isConnected = false
        this.billingClient?.endConnection()
        this.billingClient = null
    }

    fun getUnacknowledgedPurchases(onComplete: (ArrayList<Purchase>) -> Unit) {
        if (billingClient == null || !isConnected || !this.billingClient!!.isReady) {
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
        this.billingClient!!.queryPurchasesAsync(params.build()) f@{ result, purchases ->
            val array = ArrayList<Purchase>()
            purchases.forEach { purchase ->
                if (!purchase.isAcknowledged || purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    array.add(purchase)
                }
            }
            onComplete(array)
        }
    }

    /**
     * Acknowledges Google Play the user successfully received the item
     * @param purchase Purchase
     * @param onComplete
     */
    private fun acknowledge(purchase: Purchase, onComplete: (isSuccessful: Boolean) -> Unit) {
        Log.d(TAG, "Acknowledging ${purchase.products}...")

        check(billingClient != null)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()

                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) foo@{ result ->

                    handleBillingResult(result, {
                        Log.d(TAG, "Acknowledging ${purchase.products} - Success")
                        onComplete(true)
                    }, {
                        Log.d(
                            TAG,
                            "Acknowledging ${purchase.products} - Failure : Service disconnected"
                        )
                        onComplete(false)
                    })
                }
            } else {
                Log.d(TAG, "Already acknowledged ${purchase.products}")
                onComplete(true)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            onComplete(false)
        } else {
            Log.d(
                TAG,
                "Acknowledging ${purchase.products} failed since the user didn't purchase the product."
            )
            onComplete(false)
        }
    }

    private fun handleBillingResult(
        result: BillingResult,
        onSuccess: (() -> Unit)? = null,
        onConnectionFailed: (() -> Unit)? = null
    ): Boolean {
        when (result.responseCode) {
            // Billing API version is not supported for the type requested
            BillingResponseCode.BILLING_UNAVAILABLE -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
            }

            // Invalid arguments provided to the API.
            BillingResponseCode.DEVELOPER_ERROR -> {
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
            }

            // Requested feature is not supported by Play Store on the current device.
            BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
            }

            // Fatal error during the API action
            BillingResponseCode.ERROR -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
            }

            // Failure to purchase since item is already owned
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
            }

            // Failure to consume since item is not owned
            BillingResponseCode.ITEM_NOT_OWNED -> {
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }

            }

            // Success
            BillingResponseCode.OK -> {
                isConnected = true
                if (onSuccess != null) {
                    Handler(Looper.getMainLooper()).post(onSuccess)
                }
                return true
            }

            // Play Store service is not connected now - potentially transient state.
            BillingResponseCode.SERVICE_DISCONNECTED -> {
                isConnected = false
                listener.onError(result.responseCode)
            }

            // The request has reached the maximum timeout before Google Play responds.
            BillingResponseCode.NETWORK_ERROR -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
                listener.onError(result.responseCode)
            }

            // Network connection is down
            BillingResponseCode.SERVICE_UNAVAILABLE -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
                Handler(Looper.getMainLooper()).post {
                    listener.onError(result.responseCode)
                }
            }

            // User cancelled the request
            BillingResponseCode.USER_CANCELED -> {
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
            }
        }
        return false
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                listener.onProductBought(purchase)
            }
        }
    }

}