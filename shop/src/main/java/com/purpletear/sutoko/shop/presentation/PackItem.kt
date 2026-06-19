package com.purpletear.sutoko.shop.presentation

import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import androidx.annotation.Keep

/**
 * UI model that joins a shop pack with its billing product details.
 */
@Keep
data class PackItem(
    val pack: ShopPack,
    val formattedPrice: String?,
)
