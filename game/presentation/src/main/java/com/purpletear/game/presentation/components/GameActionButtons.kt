package com.purpletear.game.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.sealed.Background
import com.purpletear.game.presentation.states.ButtonUiState
import com.purpletear.game.presentation.states.GameButtonsState

/**
 * A reusable component that displays the primary action buttons for a game.
 * This component is stateless and receives all data through [state].
 *
 * @param state The UI state defining how buttons should appear and behave
 * @param onLeftClick Callback when the left button is clicked (if no click handler in state)
 * @param onRightClick Callback when the right button is clicked (if no click handler in state)
 * @param modifier Modifier for the container
 */
@Composable
internal fun GameActionButtons(
    state: GameButtonsState,
    modifier: Modifier = Modifier,
    onLeftClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null,
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    val firstButtonWeight by animateFloatAsState(
        targetValue = state.left.weight,
        animationSpec = animationSpec,
        label = "FirstButtonWeight"
    )
    val secondButtonWeight by animateFloatAsState(
        targetValue = state.right.weight,
        animationSpec = animationSpec,
        label = "SecondButtonWeight"
    )

    val animatedRightButtonColor by animateColorAsState(
        targetValue = state.right.backgroundColor,
        label = "RightButtonColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalArrangement = spacedBy(10.dp),
    ) {
        if (firstButtonWeight > 0.1f) {
            GamePreviewButton(
                modifier = Modifier.weight(firstButtonWeight),
                title = state.left.title?.asString(),
                subtitle = state.left.subtitle?.asString(),
                onClick = { state.left.onClick?.invoke() ?: onLeftClick?.invoke() },
                background = Background.Solid(Color(0xFF191919)),
                icon = state.left.icon,
                isEnabled = state.left.isEnabled,
                isLoading = state.left.isLoading,
            )
        }

        GamePreviewButton(
            modifier = Modifier.weight(secondButtonWeight),
            title = state.right.title?.asString(),
            subtitle = state.right.subtitle?.asString(),
            onClick = { state.right.onClick?.invoke() ?: onRightClick?.invoke() },
            background = Background.Solid(animatedRightButtonColor),
            icon = state.right.icon,
            isEnabled = state.right.isEnabled,
            isLoading = state.right.isLoading,
        )
    }
}

/**
 * Convenience overload that accepts individual ButtonUiState parameters.
 * Useful for previews or simple cases.
 */
@Composable
internal fun GameActionButtons(
    leftButton: ButtonUiState,
    rightButton: ButtonUiState,
    modifier: Modifier = Modifier,
    onLeftClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null,
) {
    GameActionButtons(
        state = GameButtonsState(left = leftButton, right = rightButton),
        modifier = modifier,
        onLeftClick = onLeftClick,
        onRightClick = onRightClick,
    )
}
