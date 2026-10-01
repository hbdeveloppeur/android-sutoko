package fr.purpletear.sutoko.screens.main.presentation.screens.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Only the first composed viewport participates, not later scrolls or return navigation. */
@Composable
internal fun rememberHomeEntrance(ready: Boolean): Boolean {
    var hasRevealed by rememberSaveable { mutableStateOf(false) }
    val animate = ready && !hasRevealed
    LaunchedEffect(ready) {
        if (ready && !hasRevealed) {
            // Lazy items compose during measurement, after the parent effect starts.
            // Leave the window open through that first layout before consuming it.
            withFrameNanos { }
            withFrameNanos { }
            hasRevealed = true
        }
    }
    return animate
}

@Composable
internal fun HomeEntrance(
    animate: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    // Capture the initial eligibility: the parent closes the entrance window after composition.
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (progress.value < 1f) {
            progress.animateTo(
                1f,
                tween(
                    durationMillis = 420,
                    delayMillis = delayMillis.coerceIn(0, 240),
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }
    Box(modifier.graphicsLayer {
        alpha = progress.value
    }) {
        content()
    }
}
