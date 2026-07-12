package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class ObserveCoinPurchasedSkusUseCase @Inject constructor(
    private val coinPurchaseRepository: CoinPurchaseRepository,
) {
    open operator fun invoke(): Flow<Set<String>> {
        return coinPurchaseRepository.observeCoinPurchasedSkus()
    }
}
