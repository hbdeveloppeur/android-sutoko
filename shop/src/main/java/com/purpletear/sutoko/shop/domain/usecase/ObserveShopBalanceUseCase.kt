package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveShopBalanceUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    operator fun invoke(): Flow<Balance> {
        return shopRepository.observeBalance()
    }
}
