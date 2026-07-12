package com.purpletear.sutoko.shop.domain.repository

import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for shop data access.
 *
 * Implementations are responsible for fetching, caching, and exposing
 * shop-related data (e.g. available items, offers, purchases) to the domain layer.
 */
interface ShopRepository {
    fun observeBalance(): Flow<Balance>

    fun loadBalance(
        userId: String,
        userToken: String
    ): Flow<Result<Unit>>

    /**
     * Resets the cached balance to the unloaded sentinel (`Balance(-1, -1)`).
     *
     * Postconditions:
     * - `observeBalance()` emits `Balance(coins = -1, diamonds = -1)`.
     * - `Balance.isLoaded()` returns `false`.
     *
     * Used when there is no authenticated user so the balance never reflects a
     * previous account. Implementations must perform the reset atomically.
     */
    fun resetBalance()

    /**
     * Updates the cached balance.
     *
     * Postcondition: `observeBalance()` emits the given [balance].
     */
    fun updateBalance(balance: Balance)

    suspend fun getPacks(): Result<List<ShopPack>>
}
