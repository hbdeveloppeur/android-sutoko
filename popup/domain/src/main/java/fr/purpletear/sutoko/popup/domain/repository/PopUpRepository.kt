package fr.purpletear.sutoko.popup.domain.repository

import fr.purpletear.sutoko.popup.domain.PopUp
import fr.purpletear.sutoko.popup.domain.PopUpEvent
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import kotlinx.coroutines.flow.StateFlow

interface PopUpRepository {
    val isDisplayed: StateFlow<Boolean>
    val interaction: StateFlow<PopUpEvent>
    val popUp: StateFlow<PopUp?>
    fun interact(interaction: PopUpUserInteraction)
    fun show(tag: String, popUp: PopUp)
    fun hide()
}