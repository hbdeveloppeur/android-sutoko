package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreProcessPurchaseUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    suspend operator fun invoke(
        orderId: String,
        purchaseToken: String,
        productId: String,
    ): Flow<Result<Unit>> {
        return shopRepository.preBuy(
            orderId = orderId,
            purchaseToken = purchaseToken,
            productId = productId
        )
    }
}