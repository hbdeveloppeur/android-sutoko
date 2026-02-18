package com.purpletear.shop.data.dto

import androidx.annotation.Keep

/**
 * Data class representing the response from the userHasProduct API call.
 *
 * @property granted Boolean indicating whether the user has the product.
 */
@Keep
data class UserHasProductResponseDto(
    val granted: Boolean
)