package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

open class IsStoryGrantedUseCase @Inject constructor(
    private val coinPurchaseRepository: CoinPurchaseRepository,
    private val userRepository: UserRepository,
) {
    /**
     * Contract: [Result.success] is only returned for a definitive server
     * answer (granted or not). A missing/not-yet-loaded user is a [Result.failure]
     * so callers can retry instead of treating it as "not granted".
     *
     * The timeout is a safety net: callers gate on `isUserConnected`, so the
     * user row is normally already persisted and emitted immediately.
     */
    open suspend operator fun invoke(skuIdentifiers: List<String>): Result<Boolean> {
        val user = withTimeoutOrNull(USER_TIMEOUT_MS) {
            userRepository.observeUser().filterNotNull().firstOrNull()
        } ?: return Result.failure(BuyStoryError.Unknown("User not loaded in time"))

        return coinPurchaseRepository.isStoryGranted(
            userId = user.id,
            skuIdentifiers = skuIdentifiers,
        )
    }

    private companion object {
        const val USER_TIMEOUT_MS = 3_000L
    }
}
