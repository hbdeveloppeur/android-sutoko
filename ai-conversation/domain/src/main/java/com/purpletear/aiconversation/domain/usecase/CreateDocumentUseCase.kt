package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
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