package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiTokensState
import com.purpletear.aiconversation.domain.repository.AiConversationShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAiTokensStateUseCase @Inject constructor(
    private val aiConversationShopRepository: AiConversationShopRepository,
) {
    operator fun invoke(userId: String): Flow<Result<AiTokensState>> {
        return aiConversationShopRepository.getAiTokenState(userId)
    }
}
