package com.purpletear.aiconversation.presentation.component.button

internal sealed class ButtonTheme(open val iconId: Int?) {
    data class Pink(override val iconId: Int? = null, val glow: Boolean = false) :
        ButtonTheme(iconId)

    data class Maroon(override val iconId: Int? = null) : ButtonTheme(iconId)
    data class WhitePill(override val iconId: Int? = null) : ButtonTheme(iconId)
    data class WhitePillArrow(override val iconId: Int? = null) : ButtonTheme(iconId)
}