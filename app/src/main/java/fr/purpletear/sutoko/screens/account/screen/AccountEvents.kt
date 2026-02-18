package fr.purpletear.sutoko.screens.account.screen

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Game

sealed class AccountEvents {
    object OnAccountButtonPressed : AccountEvents()
    object OnClickDiamonds : AccountEvents()
    object OnClickCoins : AccountEvents()

    @Keep
    data class OnAccountStateChanged(val isConnected: Boolean) : AccountEvents()
    @Keep
    data class OnGamePressed(val game: Game) : AccountEvents()
    @Keep
    data class OnShopStateChanged(val coins: Int, val diamonds: Int) : AccountEvents()

}
