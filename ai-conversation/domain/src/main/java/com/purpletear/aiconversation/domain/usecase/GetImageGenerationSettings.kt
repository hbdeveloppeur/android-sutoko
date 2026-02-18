package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.ImageGeneratorSettings
import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImageGenerationSettings @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository
) {
    suspend operator fun invoke(

    ): Flow<Result<ImageGeneratorSettings>> {
        return imageGenerationRepository.getSettings()
    }
}