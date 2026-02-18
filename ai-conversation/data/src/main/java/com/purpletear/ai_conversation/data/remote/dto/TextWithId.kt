package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep

@Keep
internal data class TextWithId(
    val id: String,
    val text: String,
    val role: String,
)