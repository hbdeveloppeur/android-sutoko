package com.purpletear.game.presentation.game_preview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.common.states.toButtonsState
import com.purpletear.game.presentation.game_preview.components.GamePreviewButton
import com.purpletear.game.presentation.model.GameActionState

/**
 * A reusable component that displays the primary action buttons for a game.
 * This component is stateless and receives all data through [gameAction].
 *
 * @param gameActionState The UI state defining how buttons should appear and behave
 * @param onAction Callback when a button action is triggered
 * @param modifier Modifier for the container
 */
@Composable
internal fun GameActionButtons(
    gameActionState: GameActionState?,
    onAction: (GamePreviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = gameActionState.toButtonsState(onAction = onAction)

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

    val animatedLeftButtonColor by animateColorAsState(
        targetValue = state.left.backgroundColor,
        label = "LeftButtonColor"
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
                onClick = { state.left.onClick?.invoke() },
                background = Background.Solid(animatedLeftButtonColor),
                icon = state.left.icon,
                isEnabled = state.left.isEnabled,
                isLoading = state.left.isLoading,
            )
        }

        if (secondButtonWeight > 0.1f) {
            GamePreviewButton(
                modifier = Modifier.weight(secondButtonWeight),
                title = state.right.title?.asString(),
                subtitle = state.right.subtitle?.asString(),
                onClick = { state.right.onClick?.invoke() },
                background = Background.Solid(animatedRightButtonColor),
                icon = state.right.icon,
                isEnabled = state.right.isEnabled,
                isLoading = state.right.isLoading,
            )
        }
    }
}
