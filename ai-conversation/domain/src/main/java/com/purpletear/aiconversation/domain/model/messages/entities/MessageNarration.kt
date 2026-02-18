package com.purpletear.aiconversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import java.util.UUID

@Keep
data class MessageNarration constructor(
    val text: String,
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val hiddenState: MessageState,
) : Message(
    id = id,
    state = MessageState.Sent,
    role = MessageRole.Narrator,
    typing = MessageTyping(0, 0),
    aiCharacterId = null,
    hiddenState = hiddenState,
    timestamp = timestamp
)