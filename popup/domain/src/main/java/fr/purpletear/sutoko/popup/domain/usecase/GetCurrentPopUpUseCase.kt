package fr.purpletear.sutoko.popup.domain.usecase

import fr.purpletear.sutoko.popup.domain.PopUp
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentPopUpUseCase @Inject constructor(
    private val repository: PopUpRepository,
) {
    operator fun invoke(
    ): StateFlow<PopUp?> {
        return repository.popUp
    }
}