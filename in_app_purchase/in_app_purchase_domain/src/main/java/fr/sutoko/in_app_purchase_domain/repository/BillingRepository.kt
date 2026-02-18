package fr.sutoko.in_app_purchase_domain.repository

import fr.sutoko.in_app_purchase_domain.model.AppProductDetails
import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    suspend fun connectToGooglePlay(): Flow<Result<Unit>>
    suspend fun getProducts(identifiers: List<String>): Flow<Result<List<AppProductDetails>>>
    suspend fun startBillingFlow(identifiers: List<String>): Flow<Result<Unit>>
    suspend fun acknowledgePurchase(
        purchase: AppPurchaseDetails,
        consume: Boolean
    ): Flow<Result<Unit>>

    suspend fun getNonAcknowledgePurchase(): Flow<Result<List<AppPurchaseDetails>>>
    suspend fun hasBoughtProduct(sku: List<String>): Flow<Result<Map<String, String>>>
    suspend fun consumePurchase(sku: String): Flow<Result<Unit>>
    suspend fun getActiveSubscriptionsSkus(): Flow<Result<List<String>>>
}
