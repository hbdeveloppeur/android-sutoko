package com.purpletear.sutoko.shop.domain.repository

import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.flow.Flow

/**
 * Repository for coin-based story purchases.
 *
 * The cached SKU set is a session-scoped optimization only; the backend is the
 * source of truth. Callers must not rely on the cache across process death.
 */
interface CoinPurchaseRepository {
    /**
     * Buys a story with coins for the authenticated user.
     *
     * @return The new [Balance] on success, or a [com.purpletear.sutoko.shop.domain.error.BuyStoryError] failure.
     */
    suspend fun buyStoryWithCoins(
        sku: String,
        userId: String,
    ): Result<Balance>

    /**
     * Checks whether the user has been granted the given SKUs.
     *
     * The result is cached for the remainder of the process.
     */
    suspend fun isStoryGranted(
        userId: String,
        skuIdentifiers: List<String>,
    ): Result<Boolean>

    /**
     * Emits the set of SKUs currently cached as coin-purchased.
     */
    fun observeCoinPurchasedSkus(): Flow<Set<String>>
}
