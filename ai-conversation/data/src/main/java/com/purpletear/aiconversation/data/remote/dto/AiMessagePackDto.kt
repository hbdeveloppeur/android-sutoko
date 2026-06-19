package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.model.AiMessagePack

@Keep
data class AiMessagePackDto(
    val id: Int,
    val identifier: String,
    val tokensCount: Int,
)


fun AiMessagePackDto.toDomain(): AiMessagePack {
    return AiMessagePack(
        id = id,
        identifier = identifier,
        tokensCount = tokensCount,
    )
}