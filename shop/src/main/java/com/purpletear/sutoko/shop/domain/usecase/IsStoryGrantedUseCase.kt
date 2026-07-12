package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

open class IsStoryGrantedUseCase @Inject constructor(
    private val coinPurchaseRepository: CoinPurchaseRepository,
    private val userRepository: UserRepository,
) {
    open suspend operator fun invoke(skuIdentifiers: List<String>): Result<Boolean> {
        val user = userRepository.observeUser().firstOrNull()
            ?: return Result.success(false)

        return coinPurchaseRepository.isStoryGranted(
            userId = user.id,
            skuIdentifiers = skuIdentifiers,
        )
    }
}
