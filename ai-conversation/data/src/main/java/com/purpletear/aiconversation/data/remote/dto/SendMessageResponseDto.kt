package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.model.SendMessageResponse

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

