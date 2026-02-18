package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.Document
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveSelectedDocumentUseCase @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository,
) {
    operator fun invoke(): StateFlow<Document?> {
        return imageGenerationRepository.selectedDocument
    }
}