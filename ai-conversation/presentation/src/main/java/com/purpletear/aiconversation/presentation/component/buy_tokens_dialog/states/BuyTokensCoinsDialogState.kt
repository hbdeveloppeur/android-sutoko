package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states

import androidx.annotation.Keep

sealed class BuyTokensCoinsDialogState {
    data object Loading : BuyTokensCoinsDialogState()

    data object NotLoggedIn : BuyTokensCoinsDialogState()

    @Keep
    data class Loaded(val messages: Int) : BuyTokensCoinsDialogState()
}
