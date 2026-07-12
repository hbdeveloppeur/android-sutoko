package com.purpletear.sutoko.shop.test

import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCoinPurchaseRepository : CoinPurchaseRepository {

    private val buyResults = mutableMapOf<String, Result<Balance>>()
    private val grantResults = mutableMapOf<List<String>, Result<Boolean>>()
    private val cachedSkus = MutableStateFlow<Set<String>>(emptySet())

    fun setBuyResult(sku: String, result: Result<Balance>) {
        buyResults[sku] = result
    }

    fun setGrantResult(skuIdentifiers: List<String>, result: Result<Boolean>) {
        grantResults[skuIdentifiers] = result
    }

    fun setCachedSkus(skus: Set<String>) {
        cachedSkus.value = skus
    }

    override suspend fun buyStoryWithCoins(sku: String, userId: String): Result<Balance> {
        return buyResults[sku] ?: Result.success(Balance(coins = 0, diamonds = 0))
    }

    override suspend fun isStoryGranted(userId: String, skuIdentifiers: List<String>): Result<Boolean> {
        return grantResults[skuIdentifiers] ?: Result.success(false)
    }

    override fun observeCoinPurchasedSkus(): Flow<Set<String>> = cachedSkus.asStateFlow()
}
