package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.messages.Conversation
import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationSettingsUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        userId: String?,
        aiCharacterId: Int,
    ): Flow<Conversation?> =
        conversationRepository.getSettings(
            userId = userId,
            aiCharacterId = aiCharacterId,
        )
}