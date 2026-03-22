package com.purpletear.game.presentation.preview.state

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.sharedelements.utils.UiText
import com.purpletear.core.presentation.components.icon.Icon

/**
 * UI-specific state for the game action buttons.
 * This is a plain data class that can be used by any component without ViewModel dependency.
 */
@Keep
internal data class GameButtonsState(
    val left: ButtonUiState = ButtonUiState(),
    val right: ButtonUiState = ButtonUiState(),
    val shouldTriggerVibration: Boolean = false,
)

@Keep
internal data class ButtonUiState(
    val title: UiText? = null,
    val subtitle: UiText? = null,
    val weight: Float = 0.0001f,
    val backgroundColor: Color = Color(0xFFE71464),
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val icon: Icon? = null,
    val onClick: (() -> Unit)? = null,
)
