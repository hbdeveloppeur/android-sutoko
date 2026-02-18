package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AvatarBannerPair
import com.purpletear.aiconversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRandomAvatarAndBannerPairUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(
        isFemale: Boolean
    ): Flow<Result<AvatarBannerPair>> {
        return characterRepository.getRandomAvatarAndBannerPair(
            isFemale = isFemale
        )
    }
}