package com.purpletear.shop.domain.model

import androidx.annotation.Keep

/**
 * Data class representing the user's balance of coins and diamonds.
 *
 * @property coins The number of coins in the user's balance
 * @property diamonds The number of diamonds in the user's balance
 */
@Keep
data class Balance(
    val coins: Int,
    val diamonds: Int
)
