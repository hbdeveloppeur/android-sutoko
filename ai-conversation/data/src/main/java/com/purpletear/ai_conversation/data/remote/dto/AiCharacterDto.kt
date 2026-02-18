package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.Visibility
import com.purpletear.ai_conversation.domain.model.AiCharacter

@Keep
data class AiCharacterDto(
    val id: Int,
    val firstName: String,
    val lastName: String?,
    val description: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val createdAt: Long,
    val visibility: String,
    val statusDescription: String,
    val code: String?,
)


fun AiCharacterDto.toDomain(): AiCharacter {
    return AiCharacter(
        id = id,
        firstName = firstName,
        lastName = lastName,
        description = description,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl,
        createdAt = createdAt,
        visibility = Visibility.fromString(visibility)
            ?: throw IllegalArgumentException("Visibility $visibility not found in enum Visibility"),
        statusDescription = statusDescription,
        code = code,
    )
}