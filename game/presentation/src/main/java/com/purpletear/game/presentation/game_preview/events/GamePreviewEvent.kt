package com.purpletear.game.presentation.game_preview.events

import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.game.GameCatalog
import androidx.annotation.Keep


sealed interface GamePreviewEvent {
    data object PurchaseSuccess : GamePreviewEvent
    @Keep
    data class PlayGame(val gameId: String, val isPurchased: Boolean) : GamePreviewEvent
    @Keep
    data class OnBuyGameClicked(val gameCatalog: GameCatalog) : GamePreviewEvent
    data object OpenShop : GamePreviewEvent
    data object OpenAppStore : GamePreviewEvent
    @Keep
    data class ShowError(val error: GameUiError) : GamePreviewEvent
}
