package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states

import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.UiMessagePack
import androidx.annotation.Keep

sealed class BuyTokensDialogState {

    data object Loading : BuyTokensDialogState()
    data object Login : BuyTokensDialogState()

    @Keep
    data class Packs(val packs: List<UiMessagePack>) : BuyTokensDialogState()

    sealed class Confirm : BuyTokensDialogState() {
        @Keep
        data class Buy(val pack: UiMessagePack) : Confirm()
    }

    @Keep
    data class Success(val message: UiText) : BuyTokensDialogState()

    sealed class Error(open val message: UiText) : BuyTokensDialogState() {
        @Keep
        data class Generic(override val message: UiText) : Error(message)

        @Keep
        data class NotEnoughCoins(override val message: UiText) : Error(message)
    }
}
