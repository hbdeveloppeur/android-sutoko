package com.purpletear.sutoko.shop.domain.repository.model

import androidx.annotation.Keep

@Keep
data class Balance(
    val coins: Int,
    val diamonds: Int,
    /** True when the last load attempt failed (e.g. offline); values may be stale but valid. */
    val loadFailed: Boolean = false,
) {
    fun isLoaded(): Boolean = coins != -1 && diamonds != -1
}
