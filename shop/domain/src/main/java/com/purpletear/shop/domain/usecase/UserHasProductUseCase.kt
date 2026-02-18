package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to check if a user has specific products.
 */
class UserHasProductUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository
) {
    /**
     * Check if a user has specific products.
     *
     * @param userId The ID of the user to check
     * @param skuIdentifiers List of product identifiers to check
     * @return A Flow containing the Result of the check operation with a boolean indicating if the user has the products
     */
    suspend operator fun invoke(
        userId: String,
        skuIdentifiers: List<String>
    ): Flow<Result<Boolean>> {
        return catalogRepository.userHasProduct(
            userId = userId,
            skuIdentifiers = skuIdentifiers
        )
    }
}
