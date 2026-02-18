package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
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