package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get the user's balance of coins and diamonds.
 */
class GetShopBalanceUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository
) {
    /**
     * Get the user's balance of coins and diamonds.
     *
     * @param userId The ID of the user
     * @param userToken The authentication token of the user
     * @return A Flow containing the Result of the balance retrieval operation
     */
    suspend operator fun invoke(
        userId: String,
        userToken: String
    ): Flow<Result<Balance>> {
        return catalogRepository.getBalance(
            userId = userId,
            userToken = userToken
        )
    }
}