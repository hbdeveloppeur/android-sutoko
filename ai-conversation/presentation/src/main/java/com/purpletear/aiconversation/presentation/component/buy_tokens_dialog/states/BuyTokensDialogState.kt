package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states

import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.UiMessagePack
import androidx.annotation.Keep

sealed class BuyTokensDialogState {
    sealed class Confirm {
        data object Try : BuyTokensDialogState()
        @Keep
        data class Buy(val pack: UiMessagePack) : BuyTokensDialogState()
    }

    @Keep
    data class Success(val message: UiText) : BuyTokensDialogState()
    @Keep
    data class Error(val message: UiText) : BuyTokensDialogState() {
        @Keep
        data class NotEnoughCoins(
            val message: UiText
        ) : BuyTokensDialogState()
    }

    data object Loading : BuyTokensDialogState()
    @Keep
    data class Packs(val packs: List<UiMessagePack>) : BuyTokensDialogState()
    data object Login : BuyTokensDialogState()
}