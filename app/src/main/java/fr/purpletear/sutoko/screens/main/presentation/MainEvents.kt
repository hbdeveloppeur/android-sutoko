package fr.purpletear.sutoko.screens.main.presentation

import androidx.annotation.Keep
import com.purpletear.smsgame.activities.smsgame.objects.Story
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.screens.main.domain.popup.util.MainMenuCategory

sealed class MainEvents {

    object StartScroll : MainEvents()

    @Keep
    data class EndScroll(val firstVisibleItemIndex: Int, val lastVisibileItemIndex: Int) :
        MainEvents()

    object OnAppear : MainEvents()
    object OptionButtonPressed : MainEvents()
    object AccountButtonPressed : MainEvents()
    object DiamondButtonPressed : MainEvents()
    object CoinButtonPressed : MainEvents()

    @Keep
    data class Open(val card: Game) : MainEvents()
    object TapAiConversationMenu : MainEvents()

    @Keep
    data class TapMenu(val category: MainMenuCategory) : MainEvents()
    object TapDiamondsLabel : MainEvents()
    object TapCoinsLabel : MainEvents()
    object OnPopUpDismissed : MainEvents()

    object OnFlavorModalDismissed : MainEvents()
    data object TapShop : MainEvents()

    @Keep
    data class SwitchNews(val index: Int) : MainEvents()

    @Keep
    data class ToggleNotifications(val notificationsOn: Boolean) : MainEvents()
}
