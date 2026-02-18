package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadImageGenerationRequestsRepository @Inject constructor(
    private val imageGenerationRequestsRepository: ImageGenerationRepository
) {
    suspend operator fun invoke(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>> {
        return imageGenerationRequestsRepository.loadDocuments(
            userId = userId,
            userToken = userToken,
        )
    }

}