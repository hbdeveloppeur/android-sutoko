package fr.purpletear.sutoko.popup.domain.usecase

import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import javax.inject.Inject

class HidePopUpUseCase @Inject constructor(
    private val repository: PopUpRepository,
) {
    operator fun invoke() {
        repository.hide()
    }
}