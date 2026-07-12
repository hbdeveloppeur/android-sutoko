package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.domain.exception.NotConnectedException
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

open class BuyStoryWithCoinsUseCase @Inject constructor(
    private val coinPurchaseRepository: CoinPurchaseRepository,
    private val userRepository: UserRepository,
) {
    open suspend operator fun invoke(sku: String): Result<Balance> {
        val user = userRepository.observeUser().firstOrNull()
            ?: return Result.failure(NotConnectedException())

        return coinPurchaseRepository.buyStoryWithCoins(
            sku = sku,
            userId = user.id,
        )
    }
}
