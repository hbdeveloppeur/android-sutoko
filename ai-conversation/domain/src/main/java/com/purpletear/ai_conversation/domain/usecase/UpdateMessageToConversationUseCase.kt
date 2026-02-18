package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import javax.inject.Inject

class UpdateMessageToConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(
        message: Message, update: (Message) -> Message
    ) {
        return conversationRepository.updateMessage(message, update)
    }
}