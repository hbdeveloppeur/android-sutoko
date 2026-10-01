package fr.purpletear.sutoko.screens.main.presentation.screens.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.ui.unit.dp

/** Only the first composed viewport participates, not later scrolls or return navigation. */
@Composable
internal fun rememberHomeContentEntrance(hasStories: Boolean): Boolean {
    var hasRevealedStories by rememberSaveable { mutableStateOf(false) }
    val animate = hasStories && !hasRevealedStories
    LaunchedEffect(hasStories) {
        if (hasStories) hasRevealedStories = true
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
                    durationMillis = 260,
                    delayMillis = delayMillis.coerceIn(0, 120),
                    easing = LinearOutSlowInEasing,
                ),
            )
        }
    }
    Box(modifier.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 8.dp.toPx()
    }) {
        content()
    }
}
