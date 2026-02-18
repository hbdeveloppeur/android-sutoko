package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageNarration
import com.purpletear.aiconversation.domain.model.messages.entities.MessageText
import com.purpletear.aiconversation.domain.model.messages.entities.MessageTyping
import javax.inject.Inject

class TransformTextToUserMessageUseCase @Inject constructor() {
    operator fun invoke(
        text: String,
    ): Message {

        if (text.trim().startsWith("*")) {
            return MessageNarration(
                text = text,
                hiddenState = MessageState.Idle,
            )
        }
        return MessageText(
            text = text,
            state = MessageState.PreSending,
            role = MessageRole.User,
            typing = MessageTyping(0, 0),
            aiCharacterId = null,
            hiddenState = MessageState.Idle,
            timestamp = System.currentTimeMillis(),
        )
    }
}