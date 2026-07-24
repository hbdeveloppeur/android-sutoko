package com.purpletear.game.presentation.game_preview.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R
import kotlinx.coroutines.launch

/**
 * Top-right star toggle of the GamePreview screen.
 * Subtle scale bounce + haptic tick on tap; crossfade between the
 * selected and unselected star vectors.
 */
@Composable
fun GamePreviewFavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val description = stringResource(
        if (isFavorite) R.string.game_presentation_game_preview_remove_favorite
        else R.string.game_presentation_game_preview_add_favorite
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .semantics { contentDescription = description }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                scope.launch {
                    scale.animateTo(1f, tween(durationMillis = 120, easing = FastOutSlowInEasing))
                    scale.animateTo(
                        0.8f,
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }
                onToggle()
            },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = isFavorite, label = "favoriteStar") { favorite ->
            Icon(
                painter = painterResource(
                    if (favorite) R.drawable.game_star_selected
                    else R.drawable.game_star_unselected
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
            )
        }
    }
}
