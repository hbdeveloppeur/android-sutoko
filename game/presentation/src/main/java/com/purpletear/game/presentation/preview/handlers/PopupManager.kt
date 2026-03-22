package com.purpletear.game.presentation.preview.handlers

import com.example.sharedelements.utils.UiText
import com.purpletear.game.presentation.R
import fr.purpletear.sutoko.popup.domain.PopUpIconAnimation
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import javax.inject.Inject

/**
 * Creates popup dialogs for the game preview screen.
 * The ViewModel is responsible for observing interactions via executeFlowUseCase.
 */
class PopupManager @Inject constructor(
    private val showPopUpUseCase: ShowPopUpUseCase,
) {

    /**
     * Shows a confirmation dialog before deleting a game.
     * @return The popup tag for observing interactions
     */
    fun showDeleteConfirmation(): String {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.game_delete_confirm_title),
            description = UiText.StringResource(R.string.game_delete_confirm_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_validation_green),
            buttonText = UiText.StringResource(R.string.game_delete_confirm_button)
        )
        return showPopUpUseCase(popUp)
    }

    /**
     * Shows a confirmation dialog before restarting a game.
     * @return The popup tag for observing interactions
     */
    fun showRestartConfirmation(): String {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.game_restart_confirm_title),
            description = UiText.StringResource(R.string.game_restart_confirm_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_validation_green),
            buttonText = UiText.StringResource(R.string.game_restart_confirm_button)
        )
        return showPopUpUseCase(popUp)
    }

    /**
     * Shows an alert when the game was already bought.
     * @return The popup tag for observing interactions
     */
    fun showAlreadyBoughtAlert(): String {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.already_bought_alert_title),
            description = UiText.StringResource(R.string.already_bought_alert_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_validation_green),
            buttonText = UiText.StringResource(R.string.already_bought_alert_button)
        )
        return showPopUpUseCase(popUp)
    }

    /**
     * Shows an alert for insufficient funds.
     * @return The popup tag for observing interactions
     */
    fun showInsufficientFundsAlert(): String {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.insufficient_funds_alert_title),
            description = UiText.StringResource(R.string.insufficient_funds_alert_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_treasure),
            buttonText = UiText.StringResource(R.string.insufficient_funds_alert_button)
        )
        return showPopUpUseCase(popUp)
    }
}
