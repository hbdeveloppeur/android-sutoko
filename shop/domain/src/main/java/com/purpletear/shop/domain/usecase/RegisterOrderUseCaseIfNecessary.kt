package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to register an order with the catalog service.
 */
class RegisterOrderUseCaseIfNecessary @Inject constructor(
    private val catalogRepository: CatalogRepository
) {
    /**
     * Register an order with the catalog service.
     *
     * @param purchaseToken The purchase token from the payment provider
     * @param skuIdentifier SKU identifiers for the products in the order
     * @param userId The ID of the user making the purchase
     * @param userToken The authentication token of the user
     * @return A Flow containing the Result of the registration operation
     */
    suspend operator fun invoke(
        purchaseToken: String,
        skuIdentifier: String,
        userId: String,
        userToken: String
    ): Flow<Result<Unit>> {
        return catalogRepository.registerOrder(
            purchaseToken = purchaseToken,
            skuIdentifier = skuIdentifier,
            userId = userId,
            userToken = userToken
        )
    }
}