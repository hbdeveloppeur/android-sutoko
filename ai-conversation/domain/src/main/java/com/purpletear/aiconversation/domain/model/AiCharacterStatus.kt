package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.CharacterStatus

@Keep
data class AiCharacterStatus(
    val state: CharacterStatus,
    val sinceTimeStamp: Long?,
)

