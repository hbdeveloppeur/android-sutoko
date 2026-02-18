package com.purpletear.aiconversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState

@Keep
data class MessageStoryChoiceGroup constructor(
    override val id: String,
    override val timestamp: Long,
    val choices: List<MessageStoryChoice>,
    val isConsumed: Boolean = false,
) : Message(
    id = id,
    hiddenState = MessageState.Idle,
    state = MessageState.Seen,
    role = MessageRole.Narrator,
    typing = MessageTyping(0, 0),
    aiCharacterId = null,
    timestamp = timestamp,
)


@Keep
data class MessageStoryChoice constructor(
    val id: String,
    val text: String,
    val isSelected: Boolean = false,
)