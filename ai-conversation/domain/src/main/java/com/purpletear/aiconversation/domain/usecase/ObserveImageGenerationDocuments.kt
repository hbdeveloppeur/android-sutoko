package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveImageGenerationDocuments @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository,
) {
    suspend operator fun invoke(): StateFlow<List<Document>> {
        return imageGenerationRepository.documents
    }
}