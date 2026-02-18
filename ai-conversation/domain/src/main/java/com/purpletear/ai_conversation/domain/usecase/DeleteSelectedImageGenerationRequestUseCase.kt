package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteSelectedImageGenerationRequestUseCase @Inject constructor(
    private val aiConversationSettingsRepository: ImageGenerationRepository
) {
    suspend operator fun invoke(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>> {
        return aiConversationSettingsRepository.delete(
            userId = userId,
            userToken = userToken,
        )
    }
}