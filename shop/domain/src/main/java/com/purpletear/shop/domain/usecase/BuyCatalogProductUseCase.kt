package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to buy a catalog product.
 */
class BuyCatalogProductUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository
) {
    /**
     * Buy a catalog product.
     *
     * @param userId The ID of the user making the purchase
     * @param skuIdentifier The identifier of the product to buy
     * @param type The type of the product
     * @return A Flow containing the Result of the purchase operation
     */
    suspend operator fun invoke(
        userId: String,
        skuIdentifier: String,
        type: String
    ): Flow<Result<Unit>> {
        return catalogRepository.buyCatalogProduct(
            userId = userId,
            skuIdentifier = skuIdentifier,
            type = type
        )
    }
}