package fr.purpletear.sutoko.screens.main.presentation

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.game.GameCatalog
import fr.purpletear.sutoko.screens.main.domain.popup.util.MainMenuCategory

sealed class MainEvents {

    object StartScroll : MainEvents()

    @Keep
    data class EndScroll(val firstVisibleItemIndex: Int, val lastVisibileItemIndex: Int) :
        MainEvents()

    object OnAppear : MainEvents()

    @Keep
    data class Open(val card: GameCatalog) : MainEvents()
    object TapAiConversationMenu : MainEvents()

    @Keep
    data class TapMenu(val category: MainMenuCategory) : MainEvents()
    object OnFlavorModalDismissed : MainEvents()
    data object TapShop : MainEvents()

    @Keep
    data class ToggleNotifications(val notificationsOn: Boolean) : MainEvents()
}
