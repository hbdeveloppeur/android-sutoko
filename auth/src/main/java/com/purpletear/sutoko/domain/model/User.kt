package com.purpletear.sutoko.domain.model

import androidx.annotation.Keep

@Keep
data class User(
    val id: String,
    val token: String,
)
