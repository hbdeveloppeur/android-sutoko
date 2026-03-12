package com.purpletear.game.data.local.dto

import com.google.gson.annotations.SerializedName

data class EdgeDto(
    val source: String,
    val target: String,
    val type: String = "futuristic",
    val data: EdgeDataDto? = null
)

data class EdgeDataDto(
    @SerializedName("edgeType")
    val edgeType: String = "Normal",
    val condition: String? = null
)
