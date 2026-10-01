package com.purpletear.game.presentation.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** Batch the artwork and its overlays, without letting a slow asset hold up the card. */
@Composable
internal fun ArtworkReveal(
    isReady: Boolean,
    isCached: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val ready by rememberUpdatedState(isReady)
    val cached by rememberUpdatedState(isCached)
    // Keep restored/returning cards visible even if Coil needs to reload an evicted image.
    var hasRevealed by rememberSaveable { mutableStateOf(false) }
    val opacity = remember { Animatable(if (hasRevealed || (isReady && isCached)) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (hasRevealed || currentCoroutineContext()[MotionDurationScale]?.scaleFactor == 0f) {
            hasRevealed = true
            opacity.snapTo(1f)
            return@LaunchedEffect
        }
        // Late images retain their own crossfade after this bounded coordination window.
        withTimeoutOrNull(400) { snapshotFlow { ready }.first { it } }
        hasRevealed = true
        if (cached) opacity.snapTo(1f)
        else opacity.animateTo(1f, tween(180, easing = LinearOutSlowInEasing))
    }
    Box(modifier.graphicsLayer { alpha = opacity.value }, content = content)
}
