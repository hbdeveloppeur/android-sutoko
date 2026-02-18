package fr.purpletear.sutoko.popup.domain.usecase

import fr.purpletear.sutoko.popup.domain.PopUp
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import java.util.UUID
import javax.inject.Inject

class ShowPopUpUseCase @Inject constructor(
    private val repository: PopUpRepository,
) {
    operator fun invoke(
        popUp: PopUp,
    ): String {
        val tag = UUID.randomUUID().toString()
        repository.show(tag = tag, popUp = popUp)
        return tag
    }
}