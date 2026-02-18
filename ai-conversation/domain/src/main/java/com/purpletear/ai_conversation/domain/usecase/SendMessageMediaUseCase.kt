package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageMediaUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        userId: String,
        token: String,
        aiCharacterId: Int,
        userName: String?,
        mediaId: Int,
        role: MessageRole,
    ): Flow<Result<Unit>> {
        return conversationRepository.sendMessageMedia(
            userId = userId,
            token = token,
            aiCharacterId = aiCharacterId,
            userName = userName,
            mediaId = mediaId,
            role = role
        )
    }
}