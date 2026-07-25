package com.purpletear.core.presentation.components.video

import android.view.TextureView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@Composable
@Preview(name = "Video", showBackground = false, showSystemUi = false)
private fun Preview() {
    val aspectRatio = 1080 / 1890f
    VideoComponent(
        url = "https://data.sutoko.app/resources/sutoko-ai/video/header.mp4",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    )
}

@Composable
fun VideoComponent(
    url: String,
    modifier: Modifier = Modifier,
    onVideoPrepared: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create ExoPlayer instance which is remembered along with the URL
    val mplayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            playWhenReady = true
            // Set media item and prepare immediately to avoid initial delay
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    // Pause while the owner is not visible so no frames are produced in background
    DisposableEffect(lifecycleOwner, mplayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> mplayer.pause()
                Lifecycle.Event.ON_RESUME -> mplayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Add and remove listener for player state callbacks
    var textureView by remember { mutableStateOf<TextureView?>(null) }
    DisposableEffect(mplayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    onVideoPrepared()
                }
            }
        }
        mplayer.addListener(listener)

        // Clean up listener, detach the surface, then release the player
        onDispose {
            mplayer.removeListener(listener)
            textureView?.let(mplayer::clearVideoTextureView)
            mplayer.release()
        }
    }

    // Integrate with AndroidView using a TextureView so it respects alpha/animations
    AndroidView(
        factory = { context ->
            TextureView(context)
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        update = { view ->
            textureView = view
            // Bind the video output to the TextureView
            mplayer.setVideoTextureView(view)
        }
    )
}
