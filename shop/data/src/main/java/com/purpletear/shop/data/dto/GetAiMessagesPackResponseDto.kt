package com.purpletear.shop.data.dto

import androidx.annotation.Keep

@Keep
data class GetAiMessagesPackResponseDto(
    val data : GetAiMessagesPackResponseInnerDto
)

@Keep
data class GetAiMessagesPackResponseInnerDto(
    val packs : List<AiMessagePackDto>
)
