package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import javax.inject.Inject

class UpdateConversationMessagesState @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(messages: List<Message>, state: MessageState) {
        return conversationRepository.mark(
            messages, state
        )
    }
}