package com.purpletear.game.presentation.game_preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.runningFold

internal data class PreviewVideoPlaybackState(
    val attachPlayer: Boolean = false,
    val playWhenReady: Boolean = false,
)

/** Navigation reaches RESUMED after its entrance; keep the paused frame during its exit. */
@Composable
internal fun rememberPreviewVideoPlayback(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): PreviewVideoPlaybackState {
    val playback = remember(lifecycleOwner) {
        previewVideoPlayback(lifecycleOwner.lifecycle.currentStateFlow)
    }
    return playback.collectAsState(initial = PreviewVideoPlaybackState()).value
}

internal fun previewVideoPlayback(
    lifecycleStates: Flow<Lifecycle.State>,
): Flow<PreviewVideoPlaybackState> = lifecycleStates
    .runningFold(PreviewVideoPlaybackState()) { playback, lifecycleState ->
        val resumed = lifecycleState == Lifecycle.State.RESUMED
        PreviewVideoPlaybackState(
            attachPlayer = playback.attachPlayer || resumed,
            playWhenReady = resumed,
        )
    }
    .distinctUntilChanged()
