package com.purpletear.game.data.local.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EdgeDto(
    val source: String,
    val target: String,
    val type: String? = null,
    val data: EdgeDataDto? = null
)

@Keep
data class EdgeDataDto(
    @SerializedName("edgeType")
    val edgeType: String = "Normal",
    val condition: String? = null
)
