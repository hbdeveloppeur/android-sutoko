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
    ) : GamePreviewEvent

    data object OpenAppStore : GamePreviewEvent

    data object RequestNickName : GamePreviewEvent

    data object ShowRestartDialog : GamePreviewEvent

    @Keep
    data class ShowError(val error: GameUiError) : GamePreviewEvent
}
