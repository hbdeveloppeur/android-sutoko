package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediasByImageRequestUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        imageGenerationRequest: ImageGenerationRequest
    ): Flow<List<Media>> {
        return mediaRepository.getMediasFromImageRequest(
            imageGenerationRequest
        )
    }
}