package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.CharacterStatus
import com.purpletear.aiconversation.domain.model.AiCharacterStatus

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