package com.purpletear.aiconversation.data.remote.dto.websocket

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageImage

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