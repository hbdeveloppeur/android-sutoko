package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.model.AiCustomerState
import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ProcessPurchaseUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    suspend operator fun invoke(
        userId : String,
        userToken: String,
        orderId : String,
        purchaseToken : String,
        productId : String,
        
    ): Flow<Result<AiCustomerState>> {
        return shopRepository.buy(
            userId = userId,
            userToken = userToken,
            orderId = orderId,
            purchaseToken = purchaseToken,
            productId = productId,
        )
    }
}