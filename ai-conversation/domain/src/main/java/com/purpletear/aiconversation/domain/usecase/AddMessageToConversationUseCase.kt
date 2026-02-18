package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.repository.ConversationRepository
import javax.inject.Inject

class AddMessageToConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(
        message: Message
    ) {
        return conversationRepository.addMessage(message)
    }
}