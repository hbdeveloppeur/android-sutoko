package com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states

sealed class BuyTokensCoinsDialogState {
    data class Loading(val messages: Int) :
        BuyTokensCoinsDialogState()

    data object NotLoggedIn : BuyTokensCoinsDialogState()
    data class Loaded(val messages: Int) :
        BuyTokensCoinsDialogState()
}