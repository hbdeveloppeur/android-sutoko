package fr.purpletear.sutoko.popup.domain

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import com.example.sutokosharedelements.utils.UiText

@Stable
@Keep
data class SutokoPopUp(
    val title: UiText?,
    val icon: PopUpIcon,
    val iconHeight: Dp? = null,
    val offsetY: Dp? = null,
    val buttonText: UiText?,
    val description: UiText?
) : PopUp


@Stable
@Keep
data class EditTextPopUp(
    override val title: UiText,
    val placeholder: UiText,
) : RegularPopUp

@Stable
@Keep
data class AlertPopUp(
    override val title: UiText,
) : RegularPopUp


interface RegularPopUp : PopUp {
    val title: UiText
}

interface PopUp