package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.CharacterStatus
import com.purpletear.ai_conversation.domain.model.AiCharacterStatus

@Keep
data class CharacterStatusDto(
    val status: String,
    val sinceTimeStamp: Long?,
)

fun CharacterStatusDto.toDomain(): AiCharacterStatus {
    return AiCharacterStatus(
        state = CharacterStatus.fromString(status) ?: CharacterStatus.Online,
        sinceTimeStamp = sinceTimeStamp,
    )
}