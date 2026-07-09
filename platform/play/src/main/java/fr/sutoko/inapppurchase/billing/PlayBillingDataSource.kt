package fr.sutoko.inapppurchase.billing

import android.app.Activity
import android.os.Trace
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.purpletear.sutoko.core.android.di.ActivityProvider
import fr.sutoko.inapppurchase.billing.exception.BillingException
import fr.sutoko.inapppurchase.coroutines.IoDispatcher
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PlayBillingDataSource @Inject constructor(
    private val wrapperFactory: BillingClientWrapperFactory,
    private val activityProvider: ActivityProvider,
    private val verifier: PurchaseVerifier,
    private val catalog: BillingCatalog,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BillingDataSource {

    private val applicationScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private data class PendingPurchaseFlow(
        val productId: String,
        val deferred: CompletableDeferred<PurchaseResult>,
    )

    private val pendingPurchaseRef =
        AtomicReference<PendingPurchaseFlow?>(null)

    private val _purchaseUpdates =
        MutableSharedFlow<List<PurchaseResult>>(extraBufferCapacity = 1)
    override val purchaseUpdates: Flow<List<PurchaseResult>> =
        _purchaseUpdates.asSharedFlow()

    private val _connectionState = MutableStateFlow(false)
    override val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    completePendingWithFailure(
                        responseCode = result.responseCode,
                        message = "Purchase result OK but no purchase data returned"
                    )
                    return@PurchasesUpdatedListener
                }

                /**
                 * Important:
                 * Process purchase updates even if no purchase() coroutine is waiting.
                 * Play can redeliver purchases, pending purchases can complete later,
                 * and the app process can die during the purchase flow.
                 */
                applicationScope.launch {
                    val results = purchases.flatMap { purchase ->
                        processPurchaseSafely(purchase, shouldVerify = true)
                    }

                    _purchaseUpdates.tryEmit(results)
                    completePendingIfMatching(results)
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                pendingPurchaseRef.getAndSet(null)
                    ?.deferred
                    ?.complete(PurchaseResult.Canceled)
            }

            else -> {
                completePendingWithFailure(
                    responseCode = result.responseCode,
                    message = "Billing purchase error: ${result.debugMessage}"
                )
            }
        }
    }

    @Volatile
    private var billingClientWrapper: BillingClientWrapper? = null

    private val initMutex = Mutex()

    private suspend fun billingClient(): BillingClientWrapper {
        billingClientWrapper?.let { return it }

        return initMutex.withLock {
            billingClientWrapper?.let { return@withLock it }

            Trace.beginSection("PlayBillingDataSource.createBillingClient")
            val wrapper = withContext(Dispatchers.Main.immediate) {
                wrapperFactory.create(purchasesListener)
            }
            Trace.endSection()

            billingClientWrapper = wrapper
            wrapper
        }
    }

    private val connectionMutex = Mutex()

    private fun <T> CancellableContinuation<T>.singleShotResume(): (Result<T>) -> Unit {
        val resumed = AtomicBoolean(false)
        invokeOnCancellation { resumed.compareAndSet(false, true) }
        return { result ->
            if (resumed.compareAndSet(false, true)) {
                resumeWith(result)
            }
        }
    }

    override suspend fun purchase(sku: String): PurchaseResult = withContext(ioDispatcher) {
        val product = try {
            catalog.getProduct(sku)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return@withContext PurchaseResult.Failed(
                sku = sku,
                message = e.message ?: "Product not found",
                cause = e
            )
        }

        connectionMutex.withLock {
            ensureConnected()
        }

        val activity = activityProvider.getActivity()
        if (!activity.isUsableForBillingFlow()) {
            return@withContext PurchaseResult.Failed(
                sku = product.sku,
                message = "No available foreground Activity for purchase flow"
            )
        }

        val productDetails = try {
            queryGoogleProductDetails(product)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return@withContext PurchaseResult.Failed(
                sku = product.sku,
                message = "Failed to query product details",
                cause = e
            )
        }

        if (productDetails == null) {
            return@withContext PurchaseResult.Failed(
                sku = product.sku,
                message = "Product details not found for productId: ${product.sku}"
            )
        }

        val productDetailsParams = try {
            buildProductDetailsParams(product, productDetails)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return@withContext PurchaseResult.Failed(
                sku = product.sku,
                message = e.message ?: "Failed to build billing flow params",
                cause = e
            )
        }

        val flowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))


        val deferred = CompletableDeferred<PurchaseResult>()
        val pending = PendingPurchaseFlow(
            productId = product.sku,
            deferred = deferred
        )

        if (!pendingPurchaseRef.compareAndSet(null, pending)) {
            return@withContext PurchaseResult.Failed(
                sku = product.sku,
                message = "Another purchase flow is already in progress"
            )
        }

        try {
            val launchResult = withContext(Dispatchers.Main.immediate) {
                val activity = activityProvider.getActivity()
                if (activity == null || !activity.isUsableForBillingFlow()) {
                    return@withContext BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                        .setDebugMessage("No available foreground Activity for purchase flow")
                        .build()
                }
                billingClient().launchBillingFlow(
                    activity,
                    flowParamsBuilder.build()
                )
            }

            when (launchResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    return@withContext deferred.await()
                }

                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    /**
                     * For non-consumables/subscriptions, this can mean the user already owns it.
                     * Reconcile instead of blindly failing.
                     */
                    val reconciled = reconcilePurchases()
                    val matching = reconciled.firstOrNull {
                        it.sku == product.sku
                    }

                    return@withContext matching ?: PurchaseResult.AlreadyOwned(product.sku)
                }

                else -> {
                    return@withContext PurchaseResult.Failed(
                        sku = product.sku,
                        responseCode = launchResult.responseCode,
                        message = "Failed to launch billing flow: ${launchResult.debugMessage}"
                    )
                }
            }
        } finally {
            if (pendingPurchaseRef.compareAndSet(pending, null)) {
                deferred.cancel()
            }
        }
    }

    /**
     * Call this:
     * - on app startup,
     * - when user returns to foreground,
     * - after sign-in,
     * - after ITEM_ALREADY_OWNED,
     * - from a Restore Purchases button.
     */
    override suspend fun reconcilePurchases(): List<PurchaseResult> =
        withContext(ioDispatcher) {
            connectionMutex.withLock {
                ensureConnected()
            }

            val inAppPurchases = queryGooglePurchases(BillingClient.ProductType.INAPP)
            val subscriptionPurchases = queryGooglePurchases(BillingClient.ProductType.SUBS)

            (inAppPurchases + subscriptionPurchases)
                .flatMap { purchase -> processPurchaseSafely(purchase, shouldVerify = false) }
        }

    override suspend fun queryPurchases(): List<PurchaseReceipt> =
        withContext(ioDispatcher) {
            connectionMutex.withLock {
                ensureConnected()
            }

            val inAppPurchases = queryGooglePurchases(BillingClient.ProductType.INAPP)
            val subscriptionPurchases = queryGooglePurchases(BillingClient.ProductType.SUBS)

            (inAppPurchases + subscriptionPurchases).flatMap { purchase ->
                purchase.products.mapNotNull { productId ->
                    val product = catalog.getProduct(productId)
                    purchase.toReceipt(product)
                }
            }
        }

    override suspend fun acknowledgePurchase(token: String) {
        withContext(ioDispatcher) {
            acknowledgePurchaseInternal(token)
        }
    }

    override suspend fun consumePurchase(token: String) {
        withContext(ioDispatcher) {
            consumePurchaseInternal(token)
        }
    }

    override suspend fun queryProductDetails(sku: String): BillingProductDetails? =
        queryProductDetails(listOf(sku)).singleOrNull()

    override suspend fun queryProductDetails(skus: List<String>): List<BillingProductDetails> =
        withContext(ioDispatcher) {
            connectionMutex.withLock {
                ensureConnected()
            }

            try {
                val products = catalog.getProducts(skus)
                queryGoogleProductDetails(products)
                    .map { (product, details) ->
                        details.toBillingProductDetails(product)
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: BillingException) {
                throw e
            } catch (e: Throwable) {
                throw BillingException(
                    BillingClient.BillingResponseCode.ERROR,
                    "Failed to query product details: ${e.message}"
                )
            }
        }

    override fun close() {
        pendingPurchaseRef.getAndSet(null)
            ?.deferred
            ?.cancel()

        applicationScope.cancel()
        _connectionState.value = false
        billingClientWrapper?.endConnection()
    }

    private suspend fun processPurchaseSafely(
        purchase: Purchase,
        shouldVerify: Boolean = true,
    ): List<PurchaseResult> {
        return try {
            processPurchase(purchase, shouldVerify)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val productId = purchase.products.firstOrNull()

            listOf(
                PurchaseResult.Failed(
                    sku = productId,
                    message = e.message ?: "Failed to process purchase",
                    cause = e
                )
            )
        }
    }

    private suspend fun processPurchase(
        purchase: Purchase,
        shouldVerify: Boolean,
    ): List<PurchaseResult> {
        val productIds = purchase.products

        if (productIds.isEmpty()) {
            return listOf(
                PurchaseResult.Failed(
                    sku = null,
                    message = "Purchase contained no product ids"
                )
            )
        }

        val products = productIds.map { productId ->
            val product = catalog.getProduct(productId)
            productId to product
        }

        val receipts = products.map { (_, product) ->
            purchase.toReceipt(product)
        }

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                return receipts.map { receipt ->
                    PurchaseResult.Pending(receipt)
                }
            }

            Purchase.PurchaseState.PURCHASED -> {
                if (shouldVerify) {
                    val verificationFailures = mutableListOf<PurchaseResult.Failed>()

                    products.forEach { (_, product) ->
                        val receipt = purchase.toReceipt(product)
                        val verification = verifier.verify(receipt, product)

                        if (!verification.verified) {
                            verificationFailures += PurchaseResult.Failed(
                                sku = product.sku,
                                message = verification.message
                                    ?: "Purchase verification failed for ${product.sku}"
                            )
                        }
                    }

                    if (verificationFailures.isNotEmpty()) {
                        return verificationFailures
                    }
                }

                val distinctKinds = products.map { it.second.kind }.distinct()
                if (distinctKinds.size != 1) {
                    return products.map { (_, product) ->
                        PurchaseResult.Failed(
                            sku = product.sku,
                            message = "Mixed product kinds in one purchase are not supported"
                        )
                    }
                }

                when (distinctKinds.single()) {
                    ProductKind.CONSUMABLE -> {
                        consumePurchaseInternal(purchase.purchaseToken)
                    }

                    ProductKind.NON_CONSUMABLE,
                    ProductKind.SUBSCRIPTION -> {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchaseInternal(purchase.purchaseToken)
                        }
                    }
                }

                return receipts.map { receipt ->
                    PurchaseResult.Purchased(
                        receipt.copy(
                            acknowledged = when (distinctKinds.single()) {
                                ProductKind.CONSUMABLE -> receipt.acknowledged
                                ProductKind.NON_CONSUMABLE,
                                ProductKind.SUBSCRIPTION -> true
                            }
                        )
                    )
                }
            }

            else -> {
                return receipts.map { receipt ->
                    PurchaseResult.Failed(
                        sku = receipt.sku,
                        message = "Unsupported purchase state: ${purchase.purchaseState}"
                    )
                }
            }
        }
    }

    private fun completePendingIfMatching(results: List<PurchaseResult>) {
        val pending = pendingPurchaseRef.get() ?: return

        val matchingResult = results.firstOrNull {
            it.sku == pending.productId
        } ?: return

        if (pendingPurchaseRef.compareAndSet(pending, null)) {
            pending.deferred.complete(matchingResult)
        }
    }

    private fun completePendingWithFailure(
        responseCode: Int?,
        message: String,
    ) {
        val pending = pendingPurchaseRef.getAndSet(null) ?: return

        pending.deferred.complete(
            PurchaseResult.Failed(
                sku = pending.productId,
                responseCode = responseCode,
                message = message
            )
        )
    }

    private suspend fun queryGoogleProductDetails(
        product: BillingProduct,
    ): ProductDetails? {
        return queryGoogleProductDetails(listOf(product))[product]
    }

    private suspend fun queryGoogleProductDetails(
        products: List<BillingProduct>,
    ): Map<BillingProduct, ProductDetails> = withContext(ioDispatcher) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                products.map { product ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(product.sku)
                        .setProductType(product.productType)
                        .build()
                }
            )
            .build()

        val client = billingClient()

        suspendCancellableCoroutine { continuation ->
            val resumeOnce = continuation.singleShotResume()

            client.queryProductDetailsAsync(params) { result, queryResult ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productById = products.associateBy { it.sku }
                    resumeOnce(
                        Result.success(
                            queryResult.productDetailsList.mapNotNull { details ->
                                productById[details.productId]?.let { it to details }
                            }.toMap()
                        )
                    )
                } else {
                    resumeOnce(
                        Result.failure(
                            BillingException(
                                result.responseCode,
                                "Failed to query product details: ${result.debugMessage}"
                            )
                        )
                    )
                }
            }
        }
    }

    private fun buildProductDetailsParams(
        product: BillingProduct,
        productDetails: ProductDetails,
    ): BillingFlowParams.ProductDetailsParams {
        val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (product.kind == ProductKind.SUBSCRIPTION) {
            val offerToken = product.offerToken
                ?: productDetails.subscriptionOfferDetails
                    ?.singleOrNull()
                    ?.offerToken

            require(!offerToken.isNullOrBlank()) {
                "Subscription product ${product.sku} requires an offerToken"
            }

            builder.setOfferToken(offerToken)
        }

        return builder.build()
    }

    private suspend fun queryGooglePurchases(
        productType: String,
    ): List<Purchase> = withContext(ioDispatcher) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()

        val client = billingClient()

        suspendCancellableCoroutine { continuation ->
            val resumeOnce = continuation.singleShotResume()

            client.queryPurchasesAsync(params) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    resumeOnce(Result.success(purchases))
                } else {
                    resumeOnce(
                        Result.failure(
                            BillingException(
                                result.responseCode,
                                "Failed to query purchases: ${result.debugMessage}"
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun acknowledgePurchaseInternal(token: String) = withContext(ioDispatcher) {
        connectionMutex.withLock {
            ensureConnected()
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(token)
            .build()

        val client = billingClient()

        suspendCancellableCoroutine { continuation ->
            val resumeOnce = continuation.singleShotResume()

            client.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    resumeOnce(Result.success(Unit))
                } else {
                    resumeOnce(
                        Result.failure(
                            BillingException(
                                result.responseCode,
                                "Failed to acknowledge purchase: ${result.debugMessage}"
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun consumePurchaseInternal(token: String) = withContext(ioDispatcher) {
        connectionMutex.withLock {
            ensureConnected()
        }

        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(token)
            .build()

        val client = billingClient()

        suspendCancellableCoroutine { continuation ->
            val resumeOnce = continuation.singleShotResume()

            client.consumeAsync(params) { result, _ ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    resumeOnce(Result.success(Unit))
                } else {
                    resumeOnce(
                        Result.failure(
                            BillingException(
                                result.responseCode,
                                "Failed to consume purchase: ${result.debugMessage}"
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun ensureConnected() {
        val client = billingClient()

        if (client.isReady) {
            _connectionState.value = true
            return
        }

        val result = suspendCancellableCoroutine { continuation ->
            val resumeOnce = continuation.singleShotResume()

            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            _connectionState.value = true
                        }
                        resumeOnce(Result.success(result))
                    }

                    override fun onBillingServiceDisconnected() {
                        /**
                         * Do not do entitlement logic here.
                         * Future calls will reconnect through ensureConnected().
                         */
                        _connectionState.value = false
                        resumeOnce(
                            Result.success(
                                BillingResult.newBuilder()
                                    .setResponseCode(
                                        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
                                    )
                                    .setDebugMessage("Billing service disconnected")
                                    .build()
                            )
                        )
                    }
                }
            )
        }

        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            throw BillingException(
                result.responseCode,
                "Billing service connection failed: ${result.debugMessage}"
            )
        }
    }

    private fun ProductDetails.toBillingProductDetails(
        product: BillingProduct,
    ): BillingProductDetails {
        val price = when (product.productType) {
            BillingClient.ProductType.INAPP -> {
                oneTimePurchaseOfferDetails?.formattedPrice
                    ?: throw BillingException(
                        BillingClient.BillingResponseCode.ERROR,
                        "Product ${product.sku} has no one-time purchase price information"
                    )
            }

            BillingClient.ProductType.SUBS -> {
                val offers = subscriptionOfferDetails.orEmpty()

                val selectedOffer = product.offerToken?.let { configuredToken ->
                    offers.firstOrNull { it.offerToken == configuredToken }
                        ?: throw BillingException(
                            BillingClient.BillingResponseCode.ERROR,
                            "Configured offerToken for ${product.sku} was not returned by Google Play"
                        )
                } ?: offers.singleOrNull()
                ?: throw BillingException(
                    BillingClient.BillingResponseCode.ERROR,
                    "Subscription ${product.sku} has multiple offers; configure an offerToken"
                )

                val phases = selectedOffer.pricingPhases.pricingPhaseList

                val displayPhase = phases.lastOrNull()
                    ?: throw BillingException(
                        BillingClient.BillingResponseCode.ERROR,
                        "Subscription ${product.sku} has no pricing phases"
                    )

                displayPhase.formattedPrice
            }

            else -> {
                throw BillingException(
                    BillingClient.BillingResponseCode.ERROR,
                    "Unsupported product type for ${product.sku}: ${product.productType}"
                )
            }
        }

        return BillingProductDetails(
            sku = product.sku,
            title = title,
            description = description,
            formattedPrice = price
        )
    }

    private fun Purchase.toReceipt(product: BillingProduct): PurchaseReceipt {
        return PurchaseReceipt(
            sku = product.sku,
            purchaseToken = purchaseToken,
            purchaseTime = purchaseTime,
            acknowledged = isAcknowledged,
            purchaseState = purchaseState,
            orderId = orderId
        )
    }

    private fun Activity?.isUsableForBillingFlow(): Boolean {
        return this != null && !isFinishing && !isDestroyed
    }
}
