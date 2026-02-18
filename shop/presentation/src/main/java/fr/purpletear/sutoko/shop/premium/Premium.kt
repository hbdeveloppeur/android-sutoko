package fr.purpletear.sutoko.shop.premium

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.sharedelements.BuildConfig
import com.example.sharedelements.SutokoSharedElementsData
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import purpletear.fr.purpleteartools.Std
import java.lang.ref.WeakReference

/**
 * Handles the Premium in app purchase
 * @property isConnected Boolean
 * @property billingClient BillingClient?
 * @constructor
 */
class Premium(activity: Activity, private var listener: PremiumSubscriptorListener?) :
    PurchasesUpdatedListener, DefaultLifecycleObserver {
    var isConnected: Boolean = false
        private set

    var hasProduct: Boolean = false
        private set

    companion object {
        private const val TAG: String = "SutokoPremiumHelper"
        const val DEFAULT_SKU: String = "premium_month_9_49"
        private var loadedSku: String? = null
        private val SKU: Array<String> = arrayOf(
            "sutoko_premium_subs",
            // Monthly
            "premium_month_3_99",
            "premium_month_4_49",
            "premium_month_4_99",
            "premium_month_5_49",
            "premium_month_5_99",
            "premium_month_6_49",
            "premium_month_6_99",
            "premium_month_6_89",
            "premium_month_7_49",
            "premium_month_9_49",
            "premium_month_9_99",

            // Weekly
            "premium_week_0_99",
            "premium_week_1_49",
            "premium_week_1_99",
            "premium_week_2_49",
            "premium_week_2_99",
            "premium_week_3_49",
            "premium_week_3_99",
            "premium_week_4_49",
            "premium_week_4_99",

            // Yearly
            "sutoko_premium_yearly_69_sub"
        )
        private const val SHARED_PREF_NAME: String = "Premium_SHARED_PREF_NAME"
        private const val KEY_HAS_PREMIUM_ACCESS: String = "KEY_HAS_PREMIUM_ACCESS"
        private const val KEY = "XLM:.I0-aMlkjMPmP25o?"


        /**
         * Reads the local storage
         *
         * @param activity
         */
        fun userIsPremium(activity: Activity): Boolean {
            if (BuildConfig.DEBUG && SutokoSharedElementsData.debugHasPremiumCode == 1) {
                return true
            }
            if (BuildConfig.DEBUG && SutokoSharedElementsData.debugHasPremiumCode == 0) {
                return false
            }
            val s: SharedPreferences = activity.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
            return (s.getString(KEY_HAS_PREMIUM_ACCESS, "") == KEY)
        }

        fun loadSku(activity: Activity, then: ((sku: String) -> Unit)? = null) {
            if (loadedSku != null) {
                if (then != null) {
                    Handler(Looper.getMainLooper()).post {
                        then(loadedSku!!)
                    }
                }
                return
            }
            val remoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            val map: MutableMap<String, Any> = mutableMapOf(Pair("subscription_id", DEFAULT_SKU))
            remoteConfig.setDefaultsAsync(map)
            remoteConfig.fetchAndActivate().addOnCompleteListener(activity) remoteConfig@{ task ->


                if (!task.isSuccessful) {
                    Std.debug(
                        task.exception?.message ?: "Error occured with remote config subscription"
                    )
                }

                if (activity.isFinishing) {
                    return@remoteConfig
                }

                loadedSku = remoteConfig.getString("subscription_id")

                if (then != null) {
                    Handler(Looper.getMainLooper()).post {
                        then(loadedSku!!)
                    }
                }
            }
        }

    }

    private var activity_: WeakReference<Activity>
    private var billingClient: BillingClient? = null

    init {
        Log.d(TAG, "Initialisation")
        activity_ = WeakReference<Activity>(activity)

        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (billingClient != null) {
            billingClient?.endConnection()
            this.listener = null
            billingClient = null
        }
        super.onDestroy(owner)
    }


    /**
     * Saves the local storage
     *
     * @param activity
     */
    private fun save(activity: Activity) {
        val s: SharedPreferences = activity.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        s.edit()
            .putString(KEY_HAS_PREMIUM_ACCESS, if (hasProduct) KEY else "").apply()
    }

    private fun validate(activity: Activity) {
        hasProduct = true
        save(activity)
    }

    private fun invalidate(activity: Activity) {
        hasProduct = false
        save(activity)
    }

    private fun containsSku(id: String): Boolean {
        SKU.forEach { name ->
            if (id == name) {
                return true
            }
        }
        return false
    }

    /**
     * Determines if the user purchased the IAP
     */
    private fun queryPurchaseHistoryAsync(
        activity: Activity,
        onUnableToCheck: () -> Unit
    ): Boolean {
        check(billingClient != null)

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        billingClient!!.queryPurchasesAsync(params.build())f@{ result, purchases ->
            if (BillingResponseCode.BILLING_UNAVAILABLE == result.responseCode
                || BillingResponseCode.FEATURE_NOT_SUPPORTED == result.responseCode
                || BillingResponseCode.SERVICE_DISCONNECTED == result.responseCode
                || BillingResponseCode.NETWORK_ERROR == result.responseCode
                || BillingResponseCode.SERVICE_UNAVAILABLE == result.responseCode
                || BillingResponseCode.ERROR == result.responseCode
            ) {
                Handler(Looper.getMainLooper()).post(onUnableToCheck)
                return@f
            }

            invalidate(activity)
            purchases.forEach { product ->
                product.products.forEach { sku ->
                    if (containsSku(sku) && product.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        validate(activity)
                        return@f
                    }
                }
            }
        }
        return false
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
                if (handleBillingResult(result, onConnected, onConnectionFailed)) {
                    val activity = activity_.get()
                    if (activity != null) {
                        queryPurchaseHistoryAsync(activity) {

                        }
                    }
                }
            }
        })
    }


    /**
     * Tries to buy the premium subscription
     * @param activity Activity
     * @param onAlreadyBought Function0<Unit>
     * @param onConnectionFailed Function0<Unit>
     */
    fun buy(
        activity: Activity,
        sku: String,
        onAlreadyBought: () -> Unit,
        onConnectionFailed: () -> Unit
    ) {
        check(billingClient != null)

        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        billingClient!!.queryProductDetailsAsync(
            params.build()
        ) { billingResult, skuDetails ->

            handleBillingResult(billingResult, /* onSuccess */ {

                skuDetails.forEach { detail ->


                    if (containsSku(detail.productId)) {
                        Log.d(TAG, "Buying:success")

                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(detail)
                                .build()
                        )

                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                        val result = billingClient!!.launchBillingFlow(activity, billingFlowParams)

                        if (result.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
                            hasProduct = true
                            Handler(Looper.getMainLooper()).post(onAlreadyBought)
                        }
                    }
                }
            }, {

                Log.d(TAG, "Buying:onConnectionFailed")
                onConnectionFailed()
            })
        }
    }


    /**
     * Acknowledges Google Play the user successfully received the item
     * @param purchase Purchase
     * @param onSuccess Function0<Unit>
     */
    private fun acknowledge(purchase: Purchase, onSuccess: (() -> Unit)) {
        Log.d(TAG, "Acknowledging ${purchase.products}...")

        check(billingClient != null)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()

                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) foo@{ result ->

                    handleBillingResult(result, {
                        Log.d(TAG, "Acknowledging ${purchase.products} - Success")
                        onSuccess()
                    }, {
                        Log.d(
                            TAG,
                            "Acknowledging ${purchase.products} - Failure : Service disconnected"
                        )
                    })
                }
            } else {
                Log.d(TAG, "Already acknowledged ${purchase.products}")
            }
        } else {
            Log.d(
                TAG,
                "Acknowledging ${purchase.products} failed since the user did'nt purchase the product."
            )
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
                listener?.onBillingServicesNotAvailable()
            }

            // Invalid arguments provided to the API.
            BillingResponseCode.DEVELOPER_ERROR -> {
            }

            // Requested feature is not supported by Play Store on the current device.
            BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
            }

            // Fatal error during the API action
            BillingResponseCode.ERROR -> {
                isConnected = false
            }

            // Failure to purchase since item is already owned
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
            }

            // Failure to consume since item is not owned
            BillingResponseCode.ITEM_NOT_OWNED -> {

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
                listener?.onConnectionFailed()
            }

            // The request has reached the maximum timeout before Google Play responds.
            BillingResponseCode.NETWORK_ERROR -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
                listener?.onConnectionFailed()
            }

            // Network connection is down
            BillingResponseCode.SERVICE_UNAVAILABLE -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
                listener?.onConnectionFailed()
            }

            // User cancelled the request
            BillingResponseCode.USER_CANCELED -> {
            }
        }
        return false
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.forEach { purchase ->

            purchase.products.forEach { sku ->
                if (containsSku(sku) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    val a = activity_.get()
                    if (a?.isFinishing != false) {
                        return
                    }
                    validate(a)
                    save(a)

                    listener?.onSubscriptionGrant()
                    hasProduct = true
                }
            }
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                acknowledge(purchase) /* onSuccess */ {

                }
            }
        }
    }

}