package com.purpletear.core

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Keep
sealed class UiText {
    data class DynamicText(val text: String) : UiText()
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText()

    @Composable
    fun asString() = when (this) {
        is DynamicText -> text
        is StringResource -> stringResource(id = id, *args)
    }
}
