package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentImageGenerationRequestUseCase @Inject constructor(
    private val aiConversationSettingsRepository: ImageGenerationRepository
) {
    operator fun invoke(): StateFlow<ImageGenerationRequest?> =
        aiConversationSettingsRepository.currentImageRequest
}