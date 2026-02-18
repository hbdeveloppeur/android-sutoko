package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.model.SendMessageResponse

@Keep
data class SendMessageResponseDto(
    val priority: String,
    val tokens: Int
) {
    fun toDomain(): SendMessageResponse {
        return SendMessageResponse(
            tokens = this.tokens
        )
    }
}

