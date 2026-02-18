package com.purpletear.aiconversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState

@Keep
data class MessageImage(
    override val id: String,
    override val state: MessageState,
    override val hiddenState: MessageState,
    override val role: MessageRole,
    override val aiCharacterId: Int?,
    override val timestamp: Long,
    val url: String,
    val width: Int,
    val height: Int,
    val description: String?
) : Message(
    id = id,
    state = state,
    hiddenState = hiddenState,
    role = role,
    typing = MessageTyping(0, 0),
    aiCharacterId = aiCharacterId,
    timestamp = timestamp
)