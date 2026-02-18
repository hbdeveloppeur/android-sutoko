package com.purpletear.ai_conversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import java.io.File
import java.util.UUID

@Keep
data class MessageVocal constructor(
    override val id: String = UUID.randomUUID().toString(),
    override val state: MessageState = MessageState.Sent,
    override val hiddenState: MessageState = MessageState.Idle,
    override val role: MessageRole = MessageRole.User,

    override val timestamp: Long = System.currentTimeMillis(),
    val file: File? = null,
) : Message(
    id = UUID.randomUUID().toString(),
    state = state,
    hiddenState = hiddenState,
    role = role,
    typing = MessageTyping(0, 0),
    aiCharacterId = null,
    timestamp = timestamp
)