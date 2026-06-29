package com.purpletear.game.data.remote.testing.dto

import androidx.annotation.Keep

@Keep
data class JoinTestSessionResponse(
    val sessionId: String,
    val chapterSeeds: Map<String, Int>,
)
