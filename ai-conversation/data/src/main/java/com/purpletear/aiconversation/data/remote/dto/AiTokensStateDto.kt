package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.model.AiTokensState

@Keep
data class AiTokensStateDto(
    val count: Int,
    val freeTrialAvailable: Boolean,
)

fun AiTokensStateDto.toDomain(): AiTokensState {
    return AiTokensState(
        messagesCount = count,
        freeTrialAvailable = freeTrialAvailable,
    )
}