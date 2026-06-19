package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.repository.AiConversationShopRepository
import javax.inject.Inject

class TryMessagePackUseCase @Inject constructor(
    private val aiConversationShopRepository: AiConversationShopRepository,
) {
    suspend operator fun invoke(userId: String, userToken: String): Result<Unit> {
        return aiConversationShopRepository.tryMessagePack(
            userId = userId,
            userToken = userToken,
        )
    }
}
