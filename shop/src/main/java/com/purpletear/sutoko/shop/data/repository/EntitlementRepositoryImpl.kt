package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.domain.repository.CoinPurchaseRepository
import com.purpletear.sutoko.shop.domain.repository.EntitlementRepository
import fr.sutoko.inapppurchase.application.domain.model.Purchase
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntitlementRepositoryImpl @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val coinPurchaseRepository: CoinPurchaseRepository,
    private val userRepository: UserRepository,
) : EntitlementRepository {

    override fun observeIsGranted(skuIdentifiers: List<String>): Flow<Boolean> =
        combine(
            purchaseRepository.observePurchases(),
            coinPurchaseRepository.observeCoinPurchasedSkus()
        ) { purchases, coinSkus ->
            purchases.any { it.isServerPremium() } ||
                    purchases.any {
                        it.sku in skuIdentifiers &&
                                it.purchaseState == PurchaseState.PURCHASED &&
                                it.backendRegistered
                    } ||
                    skuIdentifiers.any { it in coinSkus }
        }.distinctUntilChanged()

    override fun observeGrantedSkus(): Flow<Set<String>> =
        combine(
            purchaseRepository.observePurchases(),
            coinPurchaseRepository.observeCoinPurchasedSkus()
        ) { purchases, coinSkus ->
            purchases
                .filter { it.purchaseState == PurchaseState.PURCHASED && it.backendRegistered }
                .map { it.sku }
                .toSet() + coinSkus
        }.distinctUntilChanged()

    override fun observeHasPremium(): Flow<Boolean> =
        purchaseRepository.observePurchases()
            .map { purchases ->
                purchases.any { it.isServerPremium() }
            }
            .distinctUntilChanged()

    override suspend fun refreshGrant(skuIdentifiers: List<String>): Result<Boolean> {
        if (skuIdentifiers.isEmpty()) {
            return Result.success(false)
        }

        val user = withTimeoutOrNull(USER_TIMEOUT_MS) {
            userRepository.observeUser().filterNotNull().firstOrNull()
        } ?: return Result.failure(BuyStoryError.Unknown("User not loaded in time"))

        return coinPurchaseRepository.isStoryGranted(
            userId = user.id,
            skuIdentifiers = skuIdentifiers,
        )
    }

    private fun Purchase.isServerPremium(): Boolean =
        purchaseState == PurchaseState.PURCHASED &&
                backendRegistered &&
                sku.contains("premium", ignoreCase = true)

    private companion object {
        const val TAG = "EntitlementRepository"
        const val USER_TIMEOUT_MS = 3_000L
    }
}
