package com.purpletear.aiconversation.presentation.model

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.presentation.component.blurred_message.MessagePositionInGroup

@Keep
data class UIMessage(
    val message: Message,
    val shape: MessagePositionInGroup,
    val displaysDate: Boolean,
)