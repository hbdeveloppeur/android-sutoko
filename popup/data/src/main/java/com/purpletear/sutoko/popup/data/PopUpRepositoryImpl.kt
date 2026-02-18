package com.purpletear.sutoko.popup.data

import android.util.Log
import fr.purpletear.sutoko.popup.domain.PopUp
import fr.purpletear.sutoko.popup.domain.PopUpEvent
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PopUpRepositoryImpl() : PopUpRepository {
    private var _isDisplayed = MutableStateFlow(false)
    override val isDisplayed: StateFlow<Boolean>
        get() = _isDisplayed

    private var _interaction = MutableStateFlow(PopUpEvent(null, PopUpUserInteraction.Idle))
    override val interaction: StateFlow<PopUpEvent> = _interaction

    private var tag: String? = null

    private var _popUp: MutableStateFlow<PopUp?> = MutableStateFlow(null)
    override val popUp: StateFlow<PopUp?> = _popUp

    override fun interact(interaction: PopUpUserInteraction) {
        _interaction.value = PopUpEvent(tag, interaction)
        hide()
        Log.d("CharactersTableViewModel", _interaction.value.toString())
    }

    override fun show(tag: String, popUp: PopUp) {
        _isDisplayed.value = true
        _interaction.value = PopUpEvent(this.tag, PopUpUserInteraction.Idle)
        this.tag = tag
        _popUp.value = popUp
    }

    override fun hide() {
        _isDisplayed.value = false
        tag = null
    }
}