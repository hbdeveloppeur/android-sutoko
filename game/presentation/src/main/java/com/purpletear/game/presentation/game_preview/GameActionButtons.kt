package com.purpletear.game.presentation.game_preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.common.states.toButtonsState
import com.purpletear.game.presentation.game_preview.components.GamePreviewButton
import com.purpletear.game.presentation.model.GameActionState

@Composable
internal fun GameActionButtons(
    gameActionState: GameActionState?,
    onAction: (GamePreviewAction) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AnimatedContent(
        targetState = gameActionState,
        modifier = modifier.fillMaxWidth(),
        contentKey = {
            val buttons = it.toButtonsState(onAction)
            Triple(it?.javaClass, buttons.left.weight, buttons.right.weight)
        },
        transitionSpec = {
            (fadeIn(tween(180, delayMillis = 90)) togetherWith fadeOut(tween(90)))
                .using(SizeTransform { _, _ -> tween(240) })
        },
        label = "gameActions",
    ) { actionState ->
        val state = actionState.toButtonsState(onAction)
        val acceptsInput = enabled && actionState == gameActionState

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(10.dp),
        ) {
            // Measure each arrangement at its final width so labels never wrap mid-transition.
            if (state.left.weight > 0.1f) {
                GamePreviewButton(
                    modifier = Modifier.weight(state.left.weight),
                    title = state.left.title?.asString(),
                    accessibilityLabel = if (actionState is GameActionState.ConfirmPurchase)
                        stringResource(android.R.string.cancel) else null,
                    subtitle = state.left.subtitle?.asString(),
                    onClick = state.left.onClick,
                    background = Background.Solid(state.left.backgroundColor),
                    icon = state.left.icon,
                    isEnabled = acceptsInput && state.left.isEnabled,
                    isLoading = state.left.isLoading,
                    progress = state.left.progress,
                )
            }

            if (state.right.weight > 0.1f) {
                GamePreviewButton(
                    modifier = Modifier.weight(state.right.weight),
                    title = state.right.title?.asString(),
                    subtitle = state.right.subtitle?.asString(),
                    onClick = state.right.onClick,
                    background = Background.Solid(state.right.backgroundColor),
                    icon = state.right.icon,
                    isEnabled = acceptsInput && state.right.isEnabled,
                    isLoading = state.right.isLoading,
                    progress = state.right.progress,
                )
            }
        }
    }
}
