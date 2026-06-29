package com.purpletear.game.data.remote.testing.dto

import androidx.annotation.Keep

@Keep
data class RegisterInventoryRequest(
    val assets: List<String>,
)
