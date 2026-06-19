package fr.purpletear.sutoko.popup.domain

import androidx.annotation.Keep

@Keep
sealed class PopUpUserInteraction {
    data object Idle : PopUpUserInteraction()
    data object Dismiss : PopUpUserInteraction()
    data object Confirm : PopUpUserInteraction()
    @Keep
    data class ConfirmText(val text: String) : PopUpUserInteraction()
}