package com.purpletear.game.presentation.game_preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

private const val NAVIGATION_ENTER_DELAY_MS = 720L

/**
 * Tracks whether background video should be shown.
 *
 * Delays showing the video until the navigation enter animation has finished,
 * hides it while the screen is not RESUMED, and ensures it stops on dispose.
 */
@Composable
internal fun rememberShowVideoAfterNavigation(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): Boolean {
    var showVideo by remember { mutableStateOf(false) }

    // Enter: delay to show video after navigation animation
    LaunchedEffect(Unit) {
        delay(NAVIGATION_ENTER_DELAY_MS)
        showVideo = true
    }

    // Exit: hide video as soon as screen is not RESUMED
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState != Lifecycle.State.RESUMED) {
            showVideo = false
        } else {
            delay(NAVIGATION_ENTER_DELAY_MS)
            showVideo = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            showVideo = false
        }
    }

    return showVideo
}
