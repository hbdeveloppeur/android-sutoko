package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.data.remote.dto.GetAiMessagesPackResponseInnerDto

@Keep
data class GetAiMessagesPackResponseDto(
    val data: GetAiMessagesPackResponseInnerDto?
)