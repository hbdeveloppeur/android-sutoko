package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states

import androidx.annotation.Keep

sealed class BuyTokensCoinsDialogState {
    @Keep
    data class Loading(val messages: Int) :
        BuyTokensCoinsDialogState()

    data object NotLoggedIn : BuyTokensCoinsDialogState()
    @Keep
    data class Loaded(val messages: Int) :
        BuyTokensCoinsDialogState()
}