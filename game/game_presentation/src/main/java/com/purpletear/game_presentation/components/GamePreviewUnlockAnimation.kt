package com.purpletear.game_presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

/**
 * A composable that displays an animated gradient border box with fade in/out animation.
 *
 * @param modifier Modifier to be applied to the layout
 * @param isVisible Controls the visibility of the animation with a fade effect
 * @param animationDurationMillis Duration of the fade animation in milliseconds
 */
@Composable
internal fun GamePreviewUnlockAnimation(
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    animationDurationMillis: Int = 500
) {
    // Animate the alpha value based on isVisible parameter
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "visibilityAnimation"
    )

    Box(
        Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        AnimatedGradientBorderBox(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
        )
    }
}
