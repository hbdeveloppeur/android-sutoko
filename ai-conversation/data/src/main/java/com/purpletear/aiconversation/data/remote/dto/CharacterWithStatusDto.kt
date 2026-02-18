package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.model.AiCharacterWithStatus

@Keep
data class CharacterWithStatusDto(
    val character: AiCharacterDto,
    val status: CharacterStatusDto,
)

fun CharacterWithStatusDto.toDomain(): AiCharacterWithStatus {
    return AiCharacterWithStatus(
        character = character.toDomain(),
        status = status.toDomain()
    )
}