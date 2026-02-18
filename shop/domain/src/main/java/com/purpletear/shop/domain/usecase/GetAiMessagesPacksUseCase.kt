package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.model.AiMessagePack
import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetAiMessagesPacksUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    suspend operator fun invoke(
    ): Flow<Result<List<AiMessagePack>>> {
        return shopRepository.getAiMessagesPacks()
    }
}