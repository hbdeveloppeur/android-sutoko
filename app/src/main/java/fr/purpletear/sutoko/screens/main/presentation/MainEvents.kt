package fr.purpletear.sutoko.screens.main.presentation

import androidx.annotation.Keep

sealed class MainEvents {

    object TapAiConversationMenu : MainEvents()
    object OnFlavorModalDismissed : MainEvents()
    data object TapShop : MainEvents()

    @Keep
    data class ToggleNotifications(val notificationsOn: Boolean) : MainEvents()
}
