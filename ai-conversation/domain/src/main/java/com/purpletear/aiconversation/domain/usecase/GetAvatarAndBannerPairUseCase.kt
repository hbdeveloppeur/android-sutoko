package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AvatarBannerPair
import com.purpletear.aiconversation.domain.repository.MediaRepository
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