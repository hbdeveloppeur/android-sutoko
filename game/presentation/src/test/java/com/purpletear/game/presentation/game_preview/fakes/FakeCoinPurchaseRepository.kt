package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCoinPurchaseRepository : CoinPurchaseRepository {
    override suspend fun buyStoryWithCoins(sku: String, userId: String): Result<Balance> =
        Result.success(Balance(coins = 0, diamonds = 0))

    override suspend fun isStoryGranted(userId: String, skuIdentifiers: List<String>): Result<Boolean> =
        Result.success(false)

    override fun observeCoinPurchasedSkus(): Flow<Set<String>> = flowOf(emptySet())
}
