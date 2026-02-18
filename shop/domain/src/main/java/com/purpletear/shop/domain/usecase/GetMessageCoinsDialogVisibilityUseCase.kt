package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.ShopRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


class GetMessageCoinsDialogVisibilityUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    operator fun invoke(
        
    ): StateFlow<Boolean> {
        return shopRepository.isOpen
    }
}