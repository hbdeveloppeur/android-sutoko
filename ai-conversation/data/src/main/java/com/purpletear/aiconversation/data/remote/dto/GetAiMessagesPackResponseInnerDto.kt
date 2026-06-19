package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep

@Keep
data class GetAiMessagesPackResponseInnerDto(
    val packs: List<AiMessagePackDto>?
)
