package com.purpletear.sutoko.shop.domain.repository.model

import androidx.annotation.Keep

@Keep
data class ShopPack(
    val coins: Int,
    val diamonds: Int,
    val sku: String,
    val type: CoinsPackType,
)
