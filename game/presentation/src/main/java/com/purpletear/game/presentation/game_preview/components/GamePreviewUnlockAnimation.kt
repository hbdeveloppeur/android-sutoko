package com.purpletear.game.presentation.game_preview.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharedelements.components.AnimatedGradientBorderBox

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
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier.fillMaxSize(),
        enter = fadeIn(tween(animationDurationMillis)),
        exit = fadeOut(tween(animationDurationMillis)),
    ) {
        AnimatedGradientBorderBox(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
