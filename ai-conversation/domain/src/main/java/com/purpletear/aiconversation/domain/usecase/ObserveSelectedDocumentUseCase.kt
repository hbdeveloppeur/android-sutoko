package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveSelectedDocumentUseCase @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository,
) {
    operator fun invoke(): StateFlow<Document?> {
        return imageGenerationRepository.selectedDocument
    }
}