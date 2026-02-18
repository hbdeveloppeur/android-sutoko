package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.messages.Conversation

@Keep
data class ConversationDto(
    val id: String,
    val minAppCode: Int,
    val isBlocked: Boolean,
    val mode: String,
    val startingBackgroundUrl: String,
    val character: AiCharacter,
    val characters: List<AiCharacter>?,
)

fun ConversationDto.toDomain(): Conversation {
    return Conversation(
        id = id,
        minAppCode = minAppCode,
        isBlocked = isBlocked,
        mode = ConversationMode.fromString(mode) ?: ConversationMode.Sms,
        startingBackgroundUrl = startingBackgroundUrl,
        character = character,
        characters = characters ?: emptyList(),
    )
}