package com.purpletear.ai_conversation.domain.model.messages.entities

import androidx.annotation.Keep

@Keep
data class MessageTyping(
    val durationMs: Int,
    val delayMs: Int
)