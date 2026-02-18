package com.purpletear.ai_conversation.ui.model

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.ui.component.blurred_message.MessagePositionInGroup

@Keep
data class UIMessage(
    val message: Message,
    val shape: MessagePositionInGroup,
    val displaysDate: Boolean,
)