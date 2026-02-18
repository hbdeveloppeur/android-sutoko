package com.purpletear.ai_conversation.domain.model

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.CharacterStatus

@Keep
data class AiCharacterStatus(
    val state: CharacterStatus,
    val sinceTimeStamp: Long?,
)

