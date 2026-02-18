package com.purpletear.shop.data.dto

import androidx.annotation.Keep

/**
 * Data class representing the request body for checking if a user has specific products.
 *
 * @property userId The ID of the user to check
 * @property skuIdentifiers List of product identifiers to check
 */
@Keep
data class UserHasProductRequestDto(
    val userId: String,
    val skuIdentifiers: List<String>
)