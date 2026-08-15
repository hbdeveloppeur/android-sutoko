package com.purpletear.game.presentation.game_preview.events

import androidx.annotation.Keep
import com.purpletear.game.presentation.model.GameUiError

sealed interface GamePreviewEvent {
    data object PurchaseSuccess : GamePreviewEvent

    @Keep
    data class PlayGame(
        val gameId: String,
        val legacyId: Int?,
        val isPurchased: Boolean,
        val chapterCode: String? = null,
        val isTrial: Boolean = false,
    ) : GamePreviewEvent

    data object OpenAppStore : GamePreviewEvent

    data class RequestNickName(val isTrial: Boolean = false) : GamePreviewEvent

    data object ShowRestartDialog : GamePreviewEvent

    data object OpenAccountConnection : GamePreviewEvent

    data object ShowAlreadyBoughtAlert : GamePreviewEvent

    /** Sent when the purchase fails because the user lacks coins: open the shop. */
    data object OpenShop : GamePreviewEvent

    @Keep
    data class ShowError(val error: GameUiError) : GamePreviewEvent
}
