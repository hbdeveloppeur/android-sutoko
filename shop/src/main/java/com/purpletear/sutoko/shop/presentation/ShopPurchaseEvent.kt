package com.purpletear.sutoko.shop.presentation

import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import androidx.annotation.Keep

/**
 * One-time events emitted by [ShopViewModel] during a pack purchase flow.
 */
sealed interface ShopPurchaseEvent {
    val packType: CoinsPackType

    @Keep
    data class Started(override val packType: CoinsPackType) : ShopPurchaseEvent
    @Keep
    data class Success(override val packType: CoinsPackType) : ShopPurchaseEvent
    @Keep
    data class Pending(override val packType: CoinsPackType) : ShopPurchaseEvent
    @Keep
    data class Cancelled(override val packType: CoinsPackType) : ShopPurchaseEvent
    @Keep
    data class AlreadyOwned(override val packType: CoinsPackType) : ShopPurchaseEvent
    @Keep
    data class Failed(
        override val packType: CoinsPackType,
        val reason: String?
    ) : ShopPurchaseEvent
}
