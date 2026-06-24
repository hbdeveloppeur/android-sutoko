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

    suspend fun getPacks(): Result<List<ShopPack>>
}
