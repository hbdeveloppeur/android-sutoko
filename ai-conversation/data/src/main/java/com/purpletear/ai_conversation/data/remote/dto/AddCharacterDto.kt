package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.Visibility
import com.purpletear.ai_conversation.domain.model.AiCharacter

@Keep
data class AddCharacterDto(
    val id: Int,
    val avatarUrl: String?,
    val bannerUrl: String?,
)

fun AddCharacterDto.toDomain(
    firstName: String,
    lastName: String,
    description: String,
): AiCharacter {
    return AiCharacter(
        id = id,
        firstName = firstName,
        lastName = lastName,
        description = description,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl,
        createdAt = System.currentTimeMillis(),
        visibility = Visibility.Private,
        statusDescription = null,
        code = null,
    )
}