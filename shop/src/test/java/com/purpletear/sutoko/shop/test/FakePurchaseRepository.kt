package com.purpletear.sutoko.shop.test

import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.Purchase
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakePurchaseRepository : PurchaseRepository {

    val connectionStateFlow = MutableStateFlow(false)
    override val connectionState: Flow<Boolean> = connectionStateFlow

    override val purchaseUpdates: Flow<Unit> = flowOf()

    var queryProductDetailsResult: Map<String, Result<Product>> = emptyMap()
    var purchaseResult: Result<Unit> = Result.success(Unit)

    var queryProductDetailsCallCount = 0
        private set

    override fun observePurchases(): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observePurchase(sku: String): Flow<Purchase?> = flowOf(null)
    override fun observeHasGlobalPremium(): Flow<Boolean> = flowOf(false)
    override fun observeUnregisteredPurchases(): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observePurchasedSkus(): Flow<Set<String>> = flowOf(emptySet())
    override fun observeIsPurchased(skus: List<String>): Flow<Boolean> = flowOf(false)

    override suspend fun purchase(sku: String): Result<Unit> = purchaseResult

    override suspend fun syncPurchases(): Result<Unit> = Result.success(Unit)

    override suspend fun queryProductDetails(sku: String): Result<Product> {
        queryProductDetailsCallCount++
        return queryProductDetailsResult[sku]
            ?: Result.failure(IllegalArgumentException("No product details configured for $sku"))
    }

    override suspend fun queryProductDetails(skus: List<String>): Result<List<Product>> {
        val products = mutableListOf<Product>()
        for (sku in skus) {
            val result = queryProductDetails(sku)
            if (result.isSuccess) {
                products.add(result.getOrThrow())
            } else {
                return Result.failure(result.exceptionOrNull()!!)
            }
        }
        return Result.success(products)
    }

    override suspend fun markBackendRegistered(sku: String) {}
}
