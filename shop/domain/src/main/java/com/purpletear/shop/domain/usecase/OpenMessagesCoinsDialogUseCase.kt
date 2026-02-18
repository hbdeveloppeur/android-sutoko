package com.purpletear.shop.domain.usecase

import com.purpletear.shop.domain.repository.ShopRepository
import javax.inject.Inject


class OpenMessagesCoinsDialogUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) {
    operator fun invoke(
        
    ) {
        return shopRepository.openDialog()
    }
}