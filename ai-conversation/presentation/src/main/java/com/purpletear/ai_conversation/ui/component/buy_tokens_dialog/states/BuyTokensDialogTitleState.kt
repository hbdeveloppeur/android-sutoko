package com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states

import com.purpletear.ai_conversation.presentation.R


sealed class BuyTokensDialogTitleState {
    abstract val title: Int
    abstract val message: Int?

    data object Try : BuyTokensDialogTitleState() {
        override val title: Int = R.string.ai_conversation_presentation_title_try
        override val message: Int = R.string.ai_conversation_presentation_message_default
    }

    data object Buy : BuyTokensDialogTitleState() {
        override val title: Int = R.string.ai_conversation_presentation_title_buy
        override val message: Int = R.string.ai_conversation_presentation_message_default
    }

    object Confirm {
        data object Buy : BuyTokensDialogTitleState() {
            override val title: Int = R.string.ai_conversation_presentation_title_buy
            override val message: Int =
                R.string.ai_conversation_presentation_message_default
        }
    }
}