package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.ImageGenerationRequest
import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentImageGenerationRequestUseCase @Inject constructor(
    private val aiConversationSettingsRepository: ImageGenerationRepository
) {
    operator fun invoke(): StateFlow<ImageGenerationRequest?> =
        aiConversationSettingsRepository.currentImageRequest
}