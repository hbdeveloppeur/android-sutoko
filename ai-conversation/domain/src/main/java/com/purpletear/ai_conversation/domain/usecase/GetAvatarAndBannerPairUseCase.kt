package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.AvatarBannerPair
import com.purpletear.ai_conversation.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvatarAndBannerPairUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        imageGenerationRequestSerialId: String
    ): Flow<Result<AvatarBannerPair>> {
        return mediaRepository.getAvatarAndBanner(
            imageGenerationRequestSerialId = imageGenerationRequestSerialId,
        )
    }
}