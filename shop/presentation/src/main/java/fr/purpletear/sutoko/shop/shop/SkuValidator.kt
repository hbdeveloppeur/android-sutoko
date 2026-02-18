package fr.purpletear.sutoko.shop.shop


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.example.sharedelements.BuildConfig
import com.example.sharedelements.SutokoSharedElementsData
import fr.purpletear.sutoko.shop.presentation.R
import fr.sutoko.in_app_purchase_data.utils.AppProductDetailFromGooglePlayBilling
import fr.sutoko.in_app_purchase_domain.model.AppProductDetails

interface SkuValidatorListener {
    fun onBillingServicesNotAvailable()
    fun onConnected()
    fun onConnectionFailed()
    fun onUnhandledError()
    fun onProductBought(sku: String)
    fun onProductInfoFound(productDetails: AppProductDetails?)
    fun onVerificationDone()
}

/**
 * Handles the Premium in app purchase
 * @property isConnected Boolean
 * @property billingClient BillingClient?
 * @constructor
 */
class SkuValidator(activity: Activity) : PurchasesUpdatedListener {
    var isConnected: Boolean = false
        private set
    private val listener: SkuValidatorListener
    private var activity: Activity? = activity

    companion object {
        private const val TAG: String = "SutokoPremiumHelper"
        private const val SHARED_PREF_NAME: String = "HAS_SKU_SHARED_PREF_NAME"
        private const val KEY_HAS_SKU: String = "KEY_HAS_SKU"


        /**
         * Reads the local storage
         *
         * @param activity
         * @param skus List of SKUs to check
         * @return true if user has any of the SKUs in the list
         */
        fun userHasSku(activity: Activity, skus: List<String>): Boolean {
            if (BuildConfig.DEBUG && SutokoSharedElementsData.debugHasPaidStoriesCode == 1) {
                return true
            }
            if (BuildConfig.DEBUG && SutokoSharedElementsData.debugHasPaidStoriesCode == 0) {
                return false
            }
            val s: SharedPreferences = activity.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            );

            val userSkus = s.getStringSet(KEY_HAS_SKU, null) ?: return false

            return skus.any { sku -> userSkus.contains(sku) }
        }

        /**
         * Reads the local storage
         *
         * @param activity
         * @param sku Single SKU to check
         * @return true if user has the SKU
         */
        fun userHasSku(activity: Activity, sku: String): Boolean {
            return userHasSku(activity, listOf(sku))
        }

        /**
         * Reads the local storage
         *
         * @param activity
         */
        fun userBoughtAStory(activity: Activity): Boolean {
            if (BuildConfig.DEBUG && SutokoSharedElementsData.debugHasPaidStoriesCode == 1) {
                return true
            }
            if (BuildConfig.DEBUG && SutokoSharedElementsData.debugHasPaidStoriesCode == 0) {
                return false
            }
            val s: SharedPreferences = activity.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            );

            (s.getStringSet(KEY_HAS_SKU, null))?.forEach { code ->
                if (code.startsWith("story_")) {
                    return true
                }
            }

            return false
        }

        private fun getDaysTrialValue(productDetails: ProductDetails?): Int? {
            try {
                if (productDetails == null) {
                    return null
                }
                val offer = productDetails.subscriptionOfferDetails


                val str = offer?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                    0
                )?.billingPeriod ?: return null

                return if (str.isEmpty()) null else str.substring(1, str.length - 1).toInt()
            } catch (e: Exception) {
                return null
            }
        }

        private fun getTrialPeriod(productDetails: ProductDetails?): String? {
            if (productDetails == null) {
                return null
            }

            val offer = productDetails.subscriptionOfferDetails

            offer?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                0
            )?.billingPeriod?.let {
                if (it.endsWith("W")) {
                    return "weekly"
                }
                if (it.endsWith("Y")) {
                    return "yearly"
                }
                if (it.endsWith("M")) {
                    return "monthly"
                }
                if (it.endsWith("D")) {
                    return "daily"
                }
            }
            return null
        }

        private fun getPayingPeriod(productDetails: ProductDetails?): String? {
            try {
                if (productDetails == null) {
                    return null
                }
                val phases =
                    productDetails.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList

                if (phases == null || phases.count() < 2) {
                    return null
                }

                phases[1]?.billingPeriod?.let {
                    if (it.endsWith("W")) {
                        return "weekly"
                    }
                    if (it.endsWith("Y")) {
                        return "yearly"
                    }
                    if (it.endsWith("M")) {
                        return "monthly"
                    }
                    if (it.endsWith("D")) {
                        return "daily"
                    }
                }
                return null
            } catch (e: Exception) {
                return null
            }
        }

        fun getSubscriptionPriceString(
            activity: Activity,
            productDetails: ProductDetails?
        ): String {
            try {
                if (productDetails == null) {
                    return ""
                }
                val number = getDaysTrialValue(productDetails)
                val trialPeriod = getTrialPeriod(productDetails)
                val payingPeriod = getPayingPeriod(productDetails)
                val price =
                    productDetails.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                        0
                    )?.formattedPrice
                if (number == null || trialPeriod == null || payingPeriod == null || price == null) {
                    return ""
                }

                val payingPeriodString = when (payingPeriod) {
                    "weekly" -> activity.getString(R.string.sutoko_weeks)
                    "monthly" -> activity.getString(R.string.sutoko_month)
                    "yearly" -> activity.getString(R.string.sutoko_year)
                    else -> ""
                }.lowercase()

                return when (trialPeriod) {
                    "weekly" -> activity.getString(
                        R.string.premium_sentence_buy_free_weeks,
                        number,
                        price,
                        payingPeriodString
                    )

                    "dayly" -> activity.getString(
                        R.string.premium_sentence_buy_free_days,
                        number,
                        price,
                        payingPeriodString
                    )

                    else -> activity.getString(
                        R.string.premium_sentence_buy,
                        price,
                        payingPeriodString
                    )
                }
            } catch (e: Exception) {
                return ""
            }
        }
    }

    private var billingClient: BillingClient? = null

    init {
        Log.d(TAG, "Initialisation")
        check(activity is SkuValidatorListener) { "Activity ${activity::javaClass.name} should implement ${SkuValidatorListener::class.java.canonicalName ?: "PremiumSubscriptorListener"}" }

        listener = activity
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    }


    /**
     * Saves the local storage
     *
     * @param activity
     */
    private fun addAndSave(activity: Activity, sku: String) {
        val s: SharedPreferences = activity.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        val stringSet =
            s.getStringSet(KEY_HAS_SKU, null)?.toMutableList() ?: mutableListOf<String>()
        var found = false
        stringSet.forEach {
            if (it == sku) {
                found = true
            }
        }

        if (!found) {
            stringSet.add(sku)
            s.edit()
                .putStringSet(KEY_HAS_SKU, stringSet.toSet()).apply()
        }

    }

    private fun removeAndSave(activity: Activity, sku: String) {
        val s: SharedPreferences = activity.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        val stringSet =
            s.getStringSet(KEY_HAS_SKU, null)?.toMutableList() ?: mutableListOf<String>()
        var found = false
        stringSet.forEach {
            if (it == sku) {
                found = true
            }
        }

        if (!found) {
            stringSet.remove(sku)
            s.edit()
                .putStringSet(KEY_HAS_SKU, stringSet.toSet()).apply()
        }

    }

    private fun removeAllAndSave(activity: Activity) {
        val s: SharedPreferences = activity.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        s.edit()
            .putStringSet(KEY_HAS_SKU, setOf()).apply()
    }

    private fun validate(activity: Activity, sku: String) {
        addAndSave(activity, sku)
    }


    private fun invalidate(activity: Activity, sku: String) {
        removeAndSave(activity, sku)
    }

    private fun invalidateAll(activity: Activity) {
        removeAllAndSave(activity)
    }


    /**
     * Determines if the user purchased the IAP
     */
    private fun queryPurchasesAsync(
        activity: Activity,
        onComplete: () -> Unit,
        onUnableToCheck: () -> Unit
    ): Boolean {
        check(billingClient != null)
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        billingClient!!.queryPurchasesAsync(params.build()) f@{ result, purchases ->
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


            val allSkus = arrayListOf<String>()
            purchases.forEach { product ->
                product.products.forEach { sku ->
                    if (product.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        allSkus.add(sku)
                    }
                }
            }
            invalidateAll(activity)
            allSkus.forEach {
                validate(activity, it)
            }
            Handler(Looper.getMainLooper()).post(onComplete)
            Handler(Looper.getMainLooper()).post(this.listener::onVerificationDone)
        }
        return false
    }


    /**
     * Permits to start the BillingClient connection
     * @param onConnected Function0<Unit>?
     * @param onConnectionFailed Function0<Unit>?
     */
    fun connect(
        activity: Activity,
        onConnected: (() -> Unit)? = null,
        onConnectionFailed: (() -> Unit)? = null
    ) {
        check(billingClient != null)
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isConnected = false
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                handleBillingResult(result, {
                    queryPurchasesAsync(activity, {
                        if (onConnected != null) {
                            this@SkuValidator.listener.onConnected()
                            onConnected()
                        }
                    }) {}
                }, onConnectionFailed)
            }
        })
    }

    fun getProduct(id: String) {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        billingClient!!.queryProductDetailsAsync(
            params.build()
        ) { billingResult, details ->
            handleBillingResult(billingResult, {
                details.forEach {
                    this.listener.onProductInfoFound(
                        AppProductDetailFromGooglePlayBilling.execute(it)
                    )
                }
            })

        }
    }

    /**
     * Tries to buy the premium subscription
     * @param activity Activity
     * @param onAlreadyBought Function0<Unit>
     * @param onConnectionFailed Function0<Unit>
     */
    fun buy(
        activity: Activity,
        id: String,
        onAlreadyBought: () -> Unit,
        onConnectionFailed: () -> Unit
    ) {

        if (!isConnected) {
            Log.d(TAG, "Connect buy since the connection failed...")
            onConnectionFailed()
            return
        }


        if (activity.isFinishing) {
            return
        }

        check(billingClient != null)

        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        billingClient!!.queryProductDetailsAsync(
            params.build()
        ) { billingResult, details ->
            handleBillingResult(billingResult, /* onSuccess */ {

                details.forEach { detail ->
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
                        addAndSave(activity, id)
                        Handler(Looper.getMainLooper()).post(onAlreadyBought)
                    }
                }
            }, {

                Log.d(TAG, "Buying:onConnectionFailed")
                onConnectionFailed()
            })

        }
    }


    /**
     * Acknowledges Google Play the user successfuly received the item
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
                Handler(Looper.getMainLooper()).post {
                    listener.onBillingServicesNotAvailable()
                }
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
                listener.onConnectionFailed()
            }

            // The request has reached the maximum timeout before Google Play responds.
            BillingResponseCode.NETWORK_ERROR -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
                listener.onConnectionFailed()
            }

            // Network connection is down
            BillingResponseCode.SERVICE_UNAVAILABLE -> {
                isConnected = false
                if (onConnectionFailed != null) {
                    Handler(Looper.getMainLooper()).post(onConnectionFailed)
                }
                Handler(Looper.getMainLooper()).post {
                    listener.onConnectionFailed()
                }
            }

            // User cancelled the request
            BillingResponseCode.USER_CANCELED -> {
            }
        }
        return false
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (activity != null && !activity!!.isFinishing) {
            invalidateAll(activity!!)
        }
        purchases?.forEach { purchase ->
            purchase.products.forEach { sku ->
                Handler(Looper.getMainLooper()).post {
                    if (activity != null && !activity!!.isFinishing && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        addAndSave(activity!!, sku)
                        listener.onProductBought(sku)
                    }
                }
            }
            acknowledge(purchase) /* onSuccess */ {

            }
        }
    }

    fun consume(activity: Activity, sku: String) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
        billingClient!!.queryPurchasesAsync(params.build()) f@{ result, purchases ->
            if (activity.isFinishing) {
                return@f
            }

            if (BillingResponseCode.BILLING_UNAVAILABLE == result.responseCode
                || BillingResponseCode.FEATURE_NOT_SUPPORTED == result.responseCode
                || BillingResponseCode.SERVICE_DISCONNECTED == result.responseCode
                || BillingResponseCode.NETWORK_ERROR == result.responseCode
                || BillingResponseCode.SERVICE_UNAVAILABLE == result.responseCode
                || BillingResponseCode.ERROR == result.responseCode
            ) {
                Toast.makeText(
                    activity.applicationContext,
                    "Unable to consume order",
                    Toast.LENGTH_LONG
                ).show()
                return@f
            }

            purchases.forEach { purchase ->
                if (purchase.products.contains(sku)) {
                    val consumeParams =
                        ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    this.billingClient!!.consumeAsync(
                        consumeParams,
                        object : ConsumeResponseListener {
                            override fun onConsumeResponse(p0: BillingResult, p1: String) {
                                if (BillingResponseCode.BILLING_UNAVAILABLE == result.responseCode
                                    || BillingResponseCode.FEATURE_NOT_SUPPORTED == result.responseCode
                                    || BillingResponseCode.SERVICE_DISCONNECTED == result.responseCode
                                    || BillingResponseCode.NETWORK_ERROR == result.responseCode
                                    || BillingResponseCode.SERVICE_UNAVAILABLE == result.responseCode
                                    || BillingResponseCode.ERROR == result.responseCode
                                ) {
                                    Toast.makeText(
                                        activity.applicationContext,
                                        "Unable to consume order",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return
                                }
                                Toast.makeText(
                                    activity.applicationContext,
                                    "Order consumed",
                                    Toast.LENGTH_LONG
                                ).show()
                                invalidate(activity, sku)
                            }

                        })
                }
            }
        }
    }

}
