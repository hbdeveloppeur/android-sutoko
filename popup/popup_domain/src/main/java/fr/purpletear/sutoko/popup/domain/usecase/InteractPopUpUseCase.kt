package fr.purpletear.sutoko.popup.domain.usecase

import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import javax.inject.Inject

class InteractPopUpUseCase @Inject constructor(
    private val repository: PopUpRepository,
) {
    operator fun invoke(
        interaction: PopUpUserInteraction,
    ) {
        return repository.interact(interaction)
    }
}