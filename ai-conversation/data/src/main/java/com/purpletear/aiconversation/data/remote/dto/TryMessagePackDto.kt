package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep

@Keep
data class TryMessagePackDto(
    val code: String,
    val pack: String,
    val newTokensCount: Int?,
)