package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.ImageGenerationRequest
import com.purpletear.aiconversation.domain.model.Media
import com.purpletear.aiconversation.domain.repository.MediaRepository
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