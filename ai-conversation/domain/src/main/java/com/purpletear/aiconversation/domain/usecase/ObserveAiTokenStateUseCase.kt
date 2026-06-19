package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiTokensState
import com.purpletear.aiconversation.domain.repository.AiConversationShopRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveAiTokenStateUseCase @Inject constructor(
    private val aiConversationShopRepository: AiConversationShopRepository,
) {
    operator fun invoke(): StateFlow<AiTokensState> {
        return aiConversationShopRepository.observeAiTokenState()
    }
}
