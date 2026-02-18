package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.MessageImage
import java.util.UUID
import javax.inject.Inject

class TransformUrlToMessageImageUseCase @Inject constructor() {
    operator fun invoke(
        url: String,
        role: MessageRole,
    ): MessageImage {
        return MessageImage(
            id = UUID.randomUUID().toString(),
            state = MessageState.Sending,
            role = role,
            aiCharacterId = null,
            timestamp = System.currentTimeMillis(),
            url = url,
            height = 1,
            width = 1,
            hiddenState = MessageState.Idle,
            description = null
        )
    }
}