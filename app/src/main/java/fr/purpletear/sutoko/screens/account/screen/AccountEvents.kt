package fr.purpletear.sutoko.screens.account.screen

import androidx.annotation.Keep
import com.purpletear.game.presentation.model.GameItem

sealed class AccountEvents {
    object OnAccountButtonPressed : AccountEvents()
    object OnClickDiamonds : AccountEvents()
    object OnClickCoins : AccountEvents()

    @Keep
    data class OnAccountStateChanged(val isConnected: Boolean) : AccountEvents()

    @Keep
    data class OnGamePressed(val game: GameItem) : AccountEvents()

    @Keep
    data class OnShopStateChanged(val coins: Int, val diamonds: Int) : AccountEvents()

}
