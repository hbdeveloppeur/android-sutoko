package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.model.AiCustomerState
import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserAccountStateUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    suspend operator fun invoke(
        userId : String,
        ): Flow<Result<AiCustomerState>> {
        return shopRepository.getUserAccountState(
            userId = userId,
        )
    }
}