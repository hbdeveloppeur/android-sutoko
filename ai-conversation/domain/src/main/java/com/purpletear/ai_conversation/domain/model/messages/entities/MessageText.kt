package com.purpletear.ai_conversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import java.util.UUID

@Keep
data class MessageText(
    val text: String,
    override val id: String = UUID.randomUUID().toString(),
    override val state: MessageState,
    override val hiddenState: MessageState,
    override val role: MessageRole,
    override val typing: MessageTyping,
    override val aiCharacterId: Int?,
    override val timestamp: Long,
) : Message(
    id = id,
    state = state,
    hiddenState = hiddenState,
    role = role,
    typing = typing,
    aiCharacterId = aiCharacterId,
    timestamp = timestamp
)
