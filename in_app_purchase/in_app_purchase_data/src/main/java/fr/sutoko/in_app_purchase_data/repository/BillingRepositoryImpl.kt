package fr.sutoko.in_app_purchase_data.repository

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import fr.sutoko.in_app_purchase_data.utils.AppProductDetailFromGooglePlayBilling
import fr.sutoko.in_app_purchase_data.utils.AppPurchaseDetailFromGooglePlayBilling
import fr.sutoko.in_app_purchase_domain.model.AppProductDetails
import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import fr.sutoko.in_app_purchase_domain.model.BillingConnectionError
import fr.sutoko.in_app_purchase_domain.repository.BillingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.lang.ref.WeakReference

class BillingRepositoryImpl(private val billingClient: BillingClient) : BillingRepository {

    private var currentActivity: WeakReference<Activity>? = null

    fun setActivity(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    fun clearActivity() {
        currentActivity?.clear()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun connectToGooglePlay(): Flow<Result<Unit>> = callbackFlow {
        if (billingClient.isReady) {
            trySend(Result.success(Unit))
            close()
            return@callbackFlow
        }
        val maxRetries = 3
        var retry = 0

        // Helper function to emit result and close flow
        fun emitResult(result: Result<Unit>) {
            trySend(result)
            close() // Close the flow after sending result
        }

        val billingClientListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        emitResult(Result.success(Unit))
                    }

                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                        emitResult(Result.failure(BillingConnectionError.ServiceUnavailable))

                    BillingClient.BillingResponseCode.NETWORK_ERROR ->
                        emitResult(Result.failure(BillingConnectionError.NetworkError))

                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
                        emitResult(Result.failure(BillingConnectionError.FeatureNotSupported))

                    BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                        emitResult(Result.failure(BillingConnectionError.DeveloperError))

                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                        emitResult(Result.failure(BillingConnectionError.BillingUnavailable))

                    else ->
                        emitResult(Result.failure(BillingConnectionError.GenericError(billingResult.debugMessage)))
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retry < maxRetries) {
                    retry++
                    billingClient.startConnection(this) // Retry
                } else {
                    emitResult(Result.failure(BillingConnectionError.ServiceDisconnected))
                }
            }
        }

        billingClient.startConnection(billingClientListener)

        // Ensure we close connection when flow is cancelled
        awaitClose {
            
        }
    }

    override suspend fun getProducts(identifiers: List<String>): Flow<Result<List<AppProductDetails>>> =
        callbackFlow {
            val productList = ArrayList<QueryProductDetailsParams.Product>()
            identifiers.forEach { identifier ->
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(identifier)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            }
            if (productList.isEmpty()) {
                trySend(Result.success(emptyList()))
                return@callbackFlow
            }
            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            val callback = ProductDetailsResponseListener { result, details ->
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                    BillingClient.BillingResponseCode.ERROR,
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.NETWORK_ERROR,
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        trySend(Result.failure(Exception(result.debugMessage)))
                    }

                    BillingClient.BillingResponseCode.OK -> {
                        try {
                            val list = details.map {
                                return@map AppProductDetailFromGooglePlayBilling.execute(it)
                            }
                            trySend(Result.success(list))
                        } catch (e: Exception) {
                            trySend(Result.failure(e))
                        }
                    }
                }
            }
            billingClient.queryProductDetailsAsync(
                params.build(), callback
            )

            awaitClose {

            }

        }

    override suspend fun startBillingFlow(identifiers: List<String>): Flow<Result<Unit>> =
        callbackFlow {
            val productList = identifiers.map { id ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }.toCollection(ArrayList())

            if (currentActivity == null || currentActivity?.get() == null) {
                trySend(Result.failure(Exception("No current activity")))
                return@callbackFlow
            }

            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)


            val callback = ProductDetailsResponseListener { result, details ->
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                    BillingClient.BillingResponseCode.ERROR,
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.NETWORK_ERROR,
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        trySend(Result.failure(Exception(result.debugMessage)))
                    }

                    BillingClient.BillingResponseCode.OK -> {
                        details.forEach { detail ->
                            val productDetailsParamsList = listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(detail)
                                    .build()
                            )

                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()

                            val launchBillingResult = billingClient.launchBillingFlow(
                                currentActivity!!.get()!!,
                                billingFlowParams
                            )

                            if (launchBillingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                                trySend(Result.failure(Exception(launchBillingResult.debugMessage)))
                            } else {
                                trySend(Result.success(Unit))
                            }
                        }
                    }
                }
            }
            billingClient.queryProductDetailsAsync(
                params.build(), callback
            )

            awaitClose {

            }

        }


    override suspend fun acknowledgePurchase(
        purchase: AppPurchaseDetails,
        consume: Boolean
    ): Flow<Result<Unit>> = callbackFlow {
        if (purchase.isAcknowledged) {
            trySend(Result.success(Unit))
            return@callbackFlow
        }

        val callback = AcknowledgePurchaseResponseListener { result ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                BillingClient.BillingResponseCode.ERROR,
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.NETWORK_ERROR,
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    trySend(Result.failure(Exception(result.debugMessage)))
                }

                BillingClient.BillingResponseCode.OK -> {
                    if (consume) {
                        consume(purchase) { result, _ ->
                            // TODO RESULT MIGHT BE FALSE
                            trySend(Result.success(Unit))
                        }
                    } else {
                        trySend(Result.success(Unit))
                    }
                }
            }
        }

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)

        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build(), callback)

        awaitClose {
        }
    }


    private fun consume(
        purchase: AppPurchaseDetails,
        loop: Int = 2,
        onComplete: (Boolean, Int?) -> Unit
    ) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        this.billingClient.consumeAsync(consumeParams, object : ConsumeResponseListener {
            override fun onConsumeResponse(result: BillingResult, str: String) {
                if (BillingClient.BillingResponseCode.BILLING_UNAVAILABLE == result.responseCode
                    || BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED == result.responseCode
                    || BillingClient.BillingResponseCode.SERVICE_DISCONNECTED == result.responseCode
                    || BillingClient.BillingResponseCode.NETWORK_ERROR == result.responseCode
                    || BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE == result.responseCode
                    || BillingClient.BillingResponseCode.ERROR == result.responseCode
                    || BillingClient.BillingResponseCode.DEVELOPER_ERROR == result.responseCode
                    || BillingClient.BillingResponseCode.ITEM_NOT_OWNED == result.responseCode
                    || BillingClient.BillingResponseCode.USER_CANCELED == result.responseCode
                ) {
                    Handler(Looper.getMainLooper()).post {
                        onComplete(false, result.responseCode)
                    }
                    return
                } else {
                    if (loop > 0) {
                        consume(purchase, loop - 1, onComplete)
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            onComplete(false, null)
                        }
                    }
                }
            }

        })
    }

    override suspend fun getNonAcknowledgePurchase(): Flow<Result<List<AppPurchaseDetails>>> =
        callbackFlow {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)

            val callback = PurchasesResponseListener { result, purchases ->
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                    BillingClient.BillingResponseCode.ERROR,
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.NETWORK_ERROR,
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        trySend(Result.failure(Exception(result.debugMessage)))
                    }

                    BillingClient.BillingResponseCode.OK -> {
                        try {
                            val list = purchases.filter {
                                (!it.isAcknowledged || it.products.any { s -> s.contains("ai_message_pack") }) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                            }

                            list.forEach { p ->
                                if (p.products.any { s -> s.contains("ai_message_pack") }) {
                                    consumePurchase(p)
                                }
                            }

                            trySend(Result.success(list.filter {
                                (!it.isAcknowledged) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                            }.map(AppPurchaseDetailFromGooglePlayBilling::execute)))
                        } catch (e: Exception) {
                            trySend(Result.failure(e))
                        }
                    }
                }
            }

            billingClient.queryPurchasesAsync(
                params.build(), callback
            )

            awaitClose {

            }
        }


    override suspend fun hasBoughtProduct(sku: List<String>): Flow<Result<Map<String, String>>> =
        callbackFlow {
            val queryPurchases = {
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                ) { billingResult, purchasesList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val purchaseTokenMap = mutableMapOf<String, String>()

                        purchasesList.forEach { purchase ->
                            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                purchase.products.forEach { productSku ->
                                    if (productSku in sku) {
                                        purchaseTokenMap[productSku] = purchase.purchaseToken
                                    }
                                }
                            }
                        }

                        trySend(Result.success(purchaseTokenMap))
                    } else {
                        trySend(Result.failure(Exception(billingResult.debugMessage)))
                    }
                }
            }

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryPurchases()
                    } else {
                        trySend(Result.failure(Exception(billingResult.debugMessage)))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    trySend(Result.failure(Exception("Billing service disconnected")))
                }
            })

            awaitClose { }
        }


    override suspend fun consumePurchase(sku: String): Flow<Result<Unit>> = callbackFlow {
        if (!billingClient.isReady) {
            trySend(Result.failure(IllegalStateException("Billing client is not ready")))
            return@callbackFlow
        }

        val params = QueryPurchasesParams.newBuilder()
            // TODO
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val callback = PurchasesResponseListener { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                BillingClient.BillingResponseCode.ERROR,
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.NETWORK_ERROR,
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    trySend(Result.failure(Exception(billingResult.debugMessage)))
                }

                BillingClient.BillingResponseCode.OK -> {
                    try {
                        val activeSkus = purchasesList
                            .filter { purchase -> purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED }
                            .filter { purchase ->
                                purchase.products.any { it.contains(sku) }
                            }

                        activeSkus.forEach {
                            billingClient.consumeAsync(
                                ConsumeParams.newBuilder()
                                    .setPurchaseToken(it.purchaseToken)
                                    .build()
                            ) { billingResult, _ ->

                            }
                        }

                        trySend(Result.success(Unit))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        }


        billingClient.queryPurchasesAsync(params, callback)
    }

    override suspend fun getActiveSubscriptionsSkus(): Flow<Result<List<String>>> = callbackFlow {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val callback = PurchasesResponseListener { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                BillingClient.BillingResponseCode.ERROR,
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.NETWORK_ERROR,
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    trySend(Result.failure(Exception(billingResult.debugMessage)))
                }

                BillingClient.BillingResponseCode.OK -> {
                    try {
                        val activeSkus = purchasesList
                            .filter { purchase -> purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED }
                            .flatMap { it.products } // products is a list of productIds
                        trySend(Result.success(activeSkus))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        }

        billingClient.queryPurchasesAsync(params, callback)

        awaitClose { }
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(
            consumeParams
        ) { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation
            }
        }
    }
}
