package com.purpletear.shop.data.dto

import androidx.annotation.Keep

/**
 * Data class representing the request body for registering an order.
 *
 * @property purchaseToken The purchase token from the payment provider
 * @property userId The ID of the user making the purchase
 * @property userToken The authentication token of the user
 */
@Keep
data class RegisterOrderRequestDto(
    val purchaseToken: String,
    val skuIdentifier: String,
    val userId: String,
    val userToken: String
)