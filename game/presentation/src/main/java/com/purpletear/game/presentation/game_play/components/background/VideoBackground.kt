package com.purpletear.game.presentation.game_play.components.background

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.purpletear.game.presentation.R
import java.io.File

/**
 * Displays a looping video background using ExoPlayer.
 * Video scales to fill the container (crop behavior) using RESIZE_MODE_ZOOM.
 * Notifies parent when playback starts and propagates errors.
 *
 * Uses TextureView instead of SurfaceView so the video participates in
 * Compose alpha transitions (e.g., NavHost fade animations).
 *
 * @param videoPath The absolute path to the video file
 * @param modifier The modifier to be applied to the component
 * @param onStarted Callback invoked once when video playback starts successfully
 * @param onError Callback invoked when video playback fails, with the error details
 */
@Composable
fun VideoBackground(
    videoPath: String,
    modifier: Modifier = Modifier,
    onStarted: () -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current
    var started by remember(videoPath) { mutableStateOf(false) }

    val exoPlayer = remember(videoPath) {
        createExoPlayer(context, videoPath)
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                (LayoutInflater.from(ctx).inflate(R.layout.video_background, null, false) as PlayerView).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(videoPath, exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && !started) {
                    started = true
                    onStarted()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onError(error)
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
}

private fun createExoPlayer(context: android.content.Context, videoPath: String): ExoPlayer {
    val file = File(videoPath)
    val uri = if (file.exists()) {
        file.toUri()
    } else {
        // Fallback: try as URI string for non-file paths
        android.net.Uri.parse(videoPath)
    }

    return ExoPlayer.Builder(context).build().apply {
        repeatMode = ExoPlayer.REPEAT_MODE_ALL
        playWhenReady = true
        volume = 0f
        setMediaItem(MediaItem.fromUri(uri))
        prepare()
    }
}
