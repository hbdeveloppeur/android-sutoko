package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep

@Keep
data class AiMessagePack(
    val id: Int,
    val identifier: String,
    val tokensCount: Int,
)