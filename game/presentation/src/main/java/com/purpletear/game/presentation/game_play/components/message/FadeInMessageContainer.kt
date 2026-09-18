package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
internal fun FadeInMessageContainer(
    animate: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = 250,
    content: @Composable () -> Unit,
) {
    var hasAppeared by rememberSaveable { mutableStateOf(false) }
    val shouldAnimate = animate && !hasAppeared
    val alpha = remember { Animatable(if (shouldAnimate) 0f else 1f) }
    LaunchedEffect(Unit) {
        hasAppeared = true
        if (shouldAnimate) alpha.animateTo(1f, tween(durationMillis))
    }

    Box(modifier.graphicsLayer { this.alpha = alpha.value }) {
        content()
    }
}
