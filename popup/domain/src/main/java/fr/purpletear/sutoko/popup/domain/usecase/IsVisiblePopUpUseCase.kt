package fr.purpletear.sutoko.popup.domain.usecase

import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class IsVisiblePopUpUseCase @Inject constructor(
    private val repository: PopUpRepository,
) {
    operator fun invoke(
    ): StateFlow<Boolean> {
        return repository.isDisplayed
    }
}