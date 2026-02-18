package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states

import com.example.sharedelements.utils.UiText
import com.purpletear.shop.domain.model.AiMessagePack

sealed class BuyTokensDialogState {
    sealed class Confirm {
        data object Try : BuyTokensDialogState()
        data class Buy(val pack: AiMessagePack) : BuyTokensDialogState()
    }

    data class Success(val message: UiText) : BuyTokensDialogState()
    data class Error(val message: UiText) : BuyTokensDialogState() {
        data class NotEnoughCoins(
            val message: UiText
        ) : BuyTokensDialogState()
    }

    data object Loading : BuyTokensDialogState()
    data class Packs(val packs: List<AiMessagePack>) : BuyTokensDialogState()
    data object Login : BuyTokensDialogState()
}