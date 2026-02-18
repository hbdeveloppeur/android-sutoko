package fr.purpletear.sutoko.popup.domain.usecase

import fr.purpletear.sutoko.popup.domain.PopUpEvent
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class GetPopUpInteractionUseCase @Inject constructor(
    private val repository: PopUpRepository,
) {

    private fun isValid(e: PopUpEvent, tag: String): Boolean {
        val condition = e.tag == tag && e.event != PopUpUserInteraction.Idle
        return condition
    }

    operator fun invoke(
        tag: String
    ): StateFlow<PopUpEvent> {
        return repository.interaction.filter {
            isValid(it, tag)
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Lazily,
            initialValue = PopUpEvent(tag, PopUpUserInteraction.Idle)
        )
    }
}