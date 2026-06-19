package com.purpletear.purchase.data

import fr.sutoko.inapppurchase.billing.BillingDataSource
import fr.sutoko.inapppurchase.billing.BillingProductDetails
import fr.sutoko.inapppurchase.billing.PurchaseReceipt
import fr.sutoko.inapppurchase.billing.PurchaseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeBillingDataSource : BillingDataSource {

    val purchaseUpdatesFlow = MutableSharedFlow<List<PurchaseResult>>(extraBufferCapacity = 1)
    val connectionStateFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    override val purchaseUpdates: Flow<List<PurchaseResult>> get() = purchaseUpdatesFlow
    override val connectionState: Flow<Boolean> get() = connectionStateFlow

    var purchaseResult: PurchaseResult = PurchaseResult.Canceled
    var reconcilePurchasesResult: List<PurchaseResult> = emptyList()
    var queryPurchasesResult: List<PurchaseReceipt> = emptyList()
    var queryProductDetailsResult: BillingProductDetails? = null
    var queryProductDetailsListResult: List<BillingProductDetails> = emptyList()

    var throwOnPurchase: Throwable? = null
    var throwOnReconcilePurchases: Throwable? = null
    var throwOnQueryPurchases: Throwable? = null
    var throwOnQueryProductDetails: Throwable? = null

    val purchaseCalls = mutableListOf<String>()
    val queryProductDetailsCalls = mutableListOf<String>()
    var reconcilePurchasesCallCount = 0
    var queryPurchasesCallCount = 0
    var acknowledgePurchaseCallCount = 0
    var consumePurchaseCallCount = 0

    override suspend fun purchase(sku: String): PurchaseResult {
        purchaseCalls += sku
        throwOnPurchase?.let { throw it }
        return purchaseResult
    }

    override suspend fun reconcilePurchases(): List<PurchaseResult> {
        reconcilePurchasesCallCount++
        throwOnReconcilePurchases?.let { throw it }
        return reconcilePurchasesResult
    }

    override suspend fun queryPurchases(): List<PurchaseReceipt> {
        queryPurchasesCallCount++
        throwOnQueryPurchases?.let { throw it }
        return queryPurchasesResult
    }

    override suspend fun acknowledgePurchase(token: String) {
        acknowledgePurchaseCallCount++
    }

    override suspend fun consumePurchase(token: String) {
        consumePurchaseCallCount++
    }

    override suspend fun queryProductDetails(sku: String): BillingProductDetails? =
        queryProductDetails(listOf(sku)).singleOrNull()

    override suspend fun queryProductDetails(skus: List<String>): List<BillingProductDetails> {
        queryProductDetailsCalls += skus.joinToString()
        throwOnQueryProductDetails?.let { throw it }
        return queryProductDetailsListResult
    }

    override fun close() = Unit
}
