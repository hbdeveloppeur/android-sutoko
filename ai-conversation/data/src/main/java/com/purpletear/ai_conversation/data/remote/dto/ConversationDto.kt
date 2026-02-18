package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.ConversationMode
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.model.messages.Conversation

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