package fr.sutoko.inapppurchase.billing

import kotlinx.coroutines.flow.Flow
import java.io.Closeable

interface BillingDataSource : Closeable {
    val purchaseUpdates: Flow<List<PurchaseResult>>
    val connectionState: Flow<Boolean>
    suspend fun purchase(sku: String): PurchaseResult
    suspend fun reconcilePurchases(): List<PurchaseResult>
    suspend fun queryPurchases(): List<PurchaseReceipt>
    suspend fun acknowledgePurchase(token: String)
    suspend fun consumePurchase(token: String)
    suspend fun queryProductDetails(
        sku: String,
    ): BillingProductDetails?

    suspend fun queryProductDetails(
        skus: List<String>,
    ): List<BillingProductDetails>

    override fun close()
}