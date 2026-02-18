package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.AvatarBannerPair
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
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