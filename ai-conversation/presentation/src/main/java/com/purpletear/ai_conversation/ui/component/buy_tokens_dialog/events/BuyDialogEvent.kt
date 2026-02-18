package com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.events

import com.purpletear.shop.domain.model.AiMessagePack

sealed interface BuyDialogEvent {

    data class OnClickMessagePack(val pack: AiMessagePack) : BuyDialogEvent

    data object OnClickBuy : BuyDialogEvent

    data object OnDismissBuyDialog : BuyDialogEvent

    data object OnClickLogin : BuyDialogEvent
    data class OnUserLoggedIn(val isSuccess: Boolean) : BuyDialogEvent

    data object OnClickShop : BuyDialogEvent

}

sealed class BuyTokensDialogAbort {
    data object Try : BuyTokensDialogAbort()
    data object Buy : BuyTokensDialogAbort()
}

sealed class BuyTokensDialogConfirm {
    data object Try : BuyTokensDialogConfirm()
}