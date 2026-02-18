package com.purpletear.shop.data.dto

import androidx.annotation.Keep

@Keep
data class TryMessagePackDto(
    val code: String,
    val pack: String,
    val newTokensCount: Int?,
)
