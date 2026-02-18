package com.purpletear.shop.data.dto

import androidx.annotation.Keep

/**
 * Data class representing the request body for buying a catalog product.
 *
 * @property skuIdentifier The identifier of the product to buy
 * @property userId The ID of the user making the purchase
 * @property type The type of the product
 */
@Keep
data class BuyCatalogProductRequestDto(
    val skuIdentifier: String,
    val userId: String,
    val type: String
)