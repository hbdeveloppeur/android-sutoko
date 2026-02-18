package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.repository.ConversationRepository
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