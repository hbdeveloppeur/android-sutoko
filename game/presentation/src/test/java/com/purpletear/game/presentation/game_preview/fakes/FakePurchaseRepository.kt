package com.purpletear.game.presentation.game_preview.fakes

import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.Purchase
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakePurchaseRepository : PurchaseRepository {
    private val purchasedSkus = MutableStateFlow<Set<String>>(emptySet())
    private val hasGlobalPremium = MutableStateFlow(false)
    private val purchaseResults = mutableMapOf<String, Result<Unit>>()

    fun setPurchasedSkus(skus: Set<String>) {
        purchasedSkus.value = skus
    }

    fun setHasGlobalPremium(value: Boolean) {
        hasGlobalPremium.value = value
    }

    fun setPurchaseResult(sku: String, result: Result<Unit>) {
        purchaseResults[sku] = result
    }

    override fun observePurchasedSkus(): Flow<Set<String>> = purchasedSkus.asStateFlow()
    override fun observeHasGlobalPremium(): Flow<Boolean> = hasGlobalPremium.asStateFlow()

    override suspend fun purchase(sku: String): Result<Unit> {
        return purchaseResults[sku] ?: Result.success(Unit).also {
            purchasedSkus.value = purchasedSkus.value + sku
        }
    }

    override val purchaseUpdates: Flow<Unit> = emptyFlow()
    override val connectionState: Flow<Boolean> = flowOf(true)
    override fun observePurchases(): Flow<List<Purchase>> = emptyFlow()
    override fun observePurchase(sku: String): Flow<Purchase?> = emptyFlow()
    override fun observeUnregisteredPurchases(): Flow<List<Purchase>> = emptyFlow()
    override fun observeIsPurchased(skus: List<String>): Flow<Boolean> = emptyFlow()
    override suspend fun syncPurchases(): Result<Unit> = Result.success(Unit)
    override suspend fun queryProductDetails(sku: String): Result<Product> = Result.failure(UnsupportedOperationException())
    override suspend fun queryProductDetails(skus: List<String>): Result<List<Product>> = Result.failure(UnsupportedOperationException())
    override suspend fun markBackendRegistered(sku: String) {}
}
