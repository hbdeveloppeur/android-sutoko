package com.purpletear.aiconversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.AiCharacter
import java.util.UUID

@Keep
data class MessageInviteCharacters constructor(
    val characters: List<AiCharacter>,
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Long = System.currentTimeMillis(),
) : Message(
    id = id,
    state = MessageState.Sent,
    role = MessageRole.Narrator,
    typing = MessageTyping(0, 0),
    aiCharacterId = null,
    timestamp = timestamp,
    hiddenState = MessageState.Idle
)