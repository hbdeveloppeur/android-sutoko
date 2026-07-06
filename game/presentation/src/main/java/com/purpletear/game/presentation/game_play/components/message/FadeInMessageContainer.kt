package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private const val FADE_IN_DURATION_MS = 250
private const val FADE_OUT_DURATION_MS = 200

@Composable
internal fun FadeInMessageContainer(
    animate: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(!animate) }
    LaunchedEffect(animate) {
        if (animate) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(FADE_IN_DURATION_MS)),
        exit = fadeOut(animationSpec = tween(FADE_OUT_DURATION_MS))
    ) {
        content()
    }
}
