package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationMessagesUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        userId: String?,
        aiCharacterId: Int,
        userToken: String?
    ): Flow<List<Message>> = conversationRepository.getMessagesStream(
        userId,
        aiCharacterId,
        userToken
    )
}