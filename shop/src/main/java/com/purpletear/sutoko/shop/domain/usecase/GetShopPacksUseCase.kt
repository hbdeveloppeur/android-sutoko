package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import javax.inject.Inject

class GetShopPacksUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    suspend operator fun invoke(): Result<List<ShopPack>> {
        return shopRepository.getPacks()
    }
}
