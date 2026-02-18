package com.purpletear.shop.data.dto

import androidx.annotation.Keep

/**
 * Data class representing the request body for getting a user's balance.
 *
 * @property userId The ID of the user
 * @property userToken The authentication token of the user
 */
@Keep
data class GetBalanceRequestDto(
    val userId: String,
    val userToken: String
)