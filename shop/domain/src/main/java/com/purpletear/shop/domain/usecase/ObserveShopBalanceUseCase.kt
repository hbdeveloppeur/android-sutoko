package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case to observe the user's balance of coins and diamonds.
 */
class ObserveShopBalanceUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository
) {
    operator fun invoke(): StateFlow<Balance?> {
        return catalogRepository.shopBalance
    }
}