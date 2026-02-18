package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep

@Keep
data class AiCharacterWithStatus(
    val character: AiCharacter,
    val status: AiCharacterStatus
)

