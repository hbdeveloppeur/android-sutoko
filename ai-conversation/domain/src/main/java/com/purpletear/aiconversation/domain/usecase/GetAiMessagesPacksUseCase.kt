package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiMessagePack
import com.purpletear.aiconversation.domain.repository.AiConversationShopRepository
import javax.inject.Inject

class GetAiMessagesPacksUseCase @Inject constructor(
    private val aiConversationShopRepository: AiConversationShopRepository,
) {
    suspend operator fun invoke(): Result<List<AiMessagePack>> {
        return aiConversationShopRepository.getAiMessagesPacks()
    }
}
