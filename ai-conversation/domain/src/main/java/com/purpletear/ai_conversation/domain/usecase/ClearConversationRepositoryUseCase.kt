package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import javax.inject.Inject

class ClearConversationRepositoryUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(
    ) {
        conversationRepository.clear()
    }
}