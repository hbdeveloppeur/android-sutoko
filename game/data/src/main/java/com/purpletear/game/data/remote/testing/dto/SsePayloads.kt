package com.purpletear.game.data.remote.testing.dto

import androidx.annotation.Keep

@Keep
data class SseConnectedPayload(
    val sessionId: String,
    val chapterSeeds: Map<String, Int>,
)

@Keep
data class SsePhonePayload(
    val phoneId: String,
    val deviceInfo: String? = null,
)

@Keep
data class SseSeedUpdatedPayload(
    val chapterId: String,
    val seed: Int,
    val packageUrl: String,
    val changedAssets: List<String> = emptyList(),
)

@Keep
data class SsePlayFromNodePayload(
    val chapterId: String,
    val nodeId: String,
    val seedAtRequest: Int,
)

@Keep
data class SseErrorPayload(
    val code: String,
    val message: String,
)
