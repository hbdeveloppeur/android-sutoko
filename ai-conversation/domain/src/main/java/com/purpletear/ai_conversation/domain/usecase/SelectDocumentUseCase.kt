package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.Document
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import javax.inject.Inject

class SelectDocumentUseCase @Inject constructor(
    private val imageGenerationRequestsRepository: ImageGenerationRepository
) {
    operator fun invoke(
        document: Document,
    ) {
        return imageGenerationRequestsRepository.selectedDocument(
            document = document
        )
    }

}