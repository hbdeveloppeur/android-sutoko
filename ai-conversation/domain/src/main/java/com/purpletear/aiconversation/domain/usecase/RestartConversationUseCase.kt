package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RestartConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(
        userId: String,
        aiCharacterId: Int,
    ): Flow<Result<Unit>> {
        return conversationRepository.restartConversation(
            userId = userId,
            aiCharacterId = aiCharacterId
        )
    }
}