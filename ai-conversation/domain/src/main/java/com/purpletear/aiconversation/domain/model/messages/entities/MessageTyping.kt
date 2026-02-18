package com.purpletear.aiconversation.domain.model.messages.entities

import androidx.annotation.Keep

@Keep
data class MessageTyping(
    val durationMs: Int,
    val delayMs: Int
)