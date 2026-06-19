package fr.sutoko.inapppurchase.application.domain.repository

import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.Purchase
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    val purchaseUpdates: Flow<Unit>
    val connectionState: Flow<Boolean>
    fun observePurchases(): Flow<List<Purchase>>
    fun observePurchase(sku: String): Flow<Purchase?>
    fun observeHasGlobalPremium(): Flow<Boolean>
    fun observeUnregisteredPurchases(): Flow<List<Purchase>>
    fun observePurchasedSkus(): Flow<Set<String>>
    fun observeIsPurchased(skus: List<String>): Flow<Boolean>
    suspend fun purchase(sku: String): Result<Unit>
    suspend fun syncPurchases(): Result<Unit>
    suspend fun queryProductDetails(sku: String): Result<Product>
    suspend fun queryProductDetails(skus: List<String>): Result<List<Product>>
    suspend fun markBackendRegistered(sku: String)
}
