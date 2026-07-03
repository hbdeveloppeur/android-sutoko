package com.purpletear.game.data.remote.testing.dto

import androidx.annotation.Keep

@Keep
data class JoinTestSessionRequest(
    val storyId: String,
    val deviceInfo: String,
    val deviceId: String,
)
