package com.purpletear.aiconversation.presentation.component.button

import androidx.annotation.Keep

internal sealed class ButtonTheme(open val iconId: Int?) {
    @Keep
    data class Pink(override val iconId: Int? = null, val glow: Boolean = false) :
        ButtonTheme(iconId)

    @Keep
    data class Maroon(override val iconId: Int? = null) : ButtonTheme(iconId)
    @Keep
    data class WhitePill(override val iconId: Int? = null) : ButtonTheme(iconId)
    @Keep
    data class WhitePillArrow(override val iconId: Int? = null) : ButtonTheme(iconId)
}