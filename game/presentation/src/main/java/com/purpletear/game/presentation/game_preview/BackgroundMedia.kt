package com.purpletear.game.presentation.game_preview

import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.purpletear.game.presentation.R

/** Silent, cropped background video. TextureView participates in Compose transitions. */
@Composable
internal fun BackgroundMedia(
    videoUrl: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true,
    onFirstFrame: () -> Unit = {},
    onError: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnFirstFrame by rememberUpdatedState(onFirstFrame)
    val currentOnError by rememberUpdatedState(onError)
    val currentPlayWhenReady by rememberUpdatedState(playWhenReady)
    var playbackPosition by rememberSaveable(videoUrl) { mutableLongStateOf(0L) }
    val player = remember(context, videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(AudioAttributes.DEFAULT, false)
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(videoUrl))
            seekTo(playbackPosition)
        }
    }

    AndroidView(
        factory = { viewContext ->
            (LayoutInflater.from(viewContext)
                .inflate(R.layout.game_presentation_video_background, null, false) as PlayerView).apply {
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        },
        update = { it.player = player },
        onRelease = { it.player = null },
        modifier = modifier.fillMaxSize(),
    )

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() = currentOnFirstFrame()

            override fun onPlayerError(error: PlaybackException) = currentOnError()
        }
        player.addListener(listener)
        onDispose {
            playbackPosition = player.currentPosition.coerceAtLeast(0L)
            player.removeListener(listener)
            player.release()
        }
    }

    DisposableEffect(player, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                playbackPosition = player.currentPosition.coerceAtLeast(0L)
                player.pause()
            }
            if (event == Lifecycle.Event.ON_RESUME && currentPlayWhenReady) {
                if (player.playbackState == Player.STATE_IDLE) player.prepare()
                player.play()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(player, playWhenReady, lifecycle) {
        val playing = playWhenReady && lifecycle.currentState == Lifecycle.State.RESUMED
        if (playing && player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
        player.playWhenReady = playing
    }
}
