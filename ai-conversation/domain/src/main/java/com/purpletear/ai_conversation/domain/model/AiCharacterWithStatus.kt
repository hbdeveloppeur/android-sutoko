package com.purpletear.ai_conversation.domain.model

import androidx.annotation.Keep

@Keep
data class AiCharacterWithStatus(
    val character: AiCharacter,
    val status: AiCharacterStatus
)

