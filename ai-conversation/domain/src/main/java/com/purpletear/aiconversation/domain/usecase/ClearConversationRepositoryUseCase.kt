package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.repository.ConversationRepository
import javax.inject.Inject

class ClearConversationRepositoryUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(
    ) {
        conversationRepository.clear()
    }
}