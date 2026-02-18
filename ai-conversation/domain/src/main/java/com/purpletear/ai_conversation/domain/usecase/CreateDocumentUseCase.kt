package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import javax.inject.Inject

class CreateDocumentUseCase @Inject constructor(
    private val aiConversationSettingsRepository: ImageGenerationRepository
) {
    operator fun invoke() {
        if (!aiConversationSettingsRepository.canCreateNewDocument()) {
            return
        }
        aiConversationSettingsRepository.createNewDocument(
            request = null
        )
    }
}