package com.purpletear.ai_conversation.data.remote.dto.websocket

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageImage

@Keep
data class AiConversationChatImageMessageDto(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val role: String,
    val timestamp: Long,
    val characterId: Int,
    val description: String?,
)

fun AiConversationChatImageMessageDto.toDomain(): Message = MessageImage(
    id = id,
    timestamp = timestamp,
    state = MessageState.Sent,
    width = width,
    height = height,
    role = MessageRole.fromString(role) ?: MessageRole.User,
    url = url,
    aiCharacterId = characterId,
    description = description,
    hiddenState = MessageState.Idle,

    )