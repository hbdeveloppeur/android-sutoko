package com.purpletear.sutoko.shop.domain.repository.model

import androidx.annotation.Keep

@Keep
data class Balance(
    val coins: Int,
    val diamonds: Int,
) {
    fun isLoaded(): Boolean = coins != -1 && diamonds != -1
}
