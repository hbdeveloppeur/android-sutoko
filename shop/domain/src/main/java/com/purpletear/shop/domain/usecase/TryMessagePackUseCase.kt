package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class TryMessagePackUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    suspend operator fun invoke(
        userId : String,
        userToken: String,
        
    ): Flow<Result<Boolean>> {
        return shopRepository.tryMessagePack(
            uid = userId,
            userToken = userToken,
        )
    }
}