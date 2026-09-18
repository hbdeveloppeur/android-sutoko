package com.purpletear.game.presentation.game_preview.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Resumed menu ambience with audio focus, volume fades and a pause between loops. */
@Composable
fun GamePreviewMenuSoundEffect(
    soundUrl: String?,
    muted: Boolean,
) {
    if (soundUrl.isNullOrBlank()) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val player = remember(context, soundUrl) {
        GamePreviewMenuSoundPlayer(context.applicationContext, soundUrl)
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    LaunchedEffect(player, muted, lifecycleState) {
        player.setPlaying(!muted && lifecycleState == Lifecycle.State.RESUMED)
    }
}

private class GamePreviewMenuSoundPlayer(context: Context, soundUrl: String) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player = ExoPlayer.Builder(context).build()
    private var fadeJob: Job? = null
    private var loopJob: Job? = null
    private var released = false
    private var wantsPlaying = false

    init {
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        player.setHandleAudioBecomingNoisy(true)
        player.volume = 0f
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) scheduleNextLoop()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (!playWhenReady && (
                        reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS ||
                            reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
                        )) {
                    loopJob?.cancel()
                    fadeJob?.cancel()
                }
            }

            override fun onPlayerError(error: PlaybackException) = release()
        })
        player.setMediaItem(MediaItem.fromUri(soundUrl))
        player.prepare()
    }

    fun setPlaying(playing: Boolean) {
        if (released || wantsPlaying == playing) return
        wantsPlaying = playing
        if (playing) startPlayback() else stopPlayback()
    }

    fun release() {
        if (released) return
        released = true
        scope.cancel()
        player.release()
    }

    private fun startPlayback() {
        loopJob?.cancel()
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.play()
        fadeTo(MAX_VOLUME)
    }

    private fun stopPlayback() {
        loopJob?.cancel()
        fadeJob?.cancel()
        fadeJob = scope.launch {
            fadeStep(0f)
            player.pause()
        }
    }

    private fun scheduleNextLoop() {
        fadeJob?.cancel()
        player.volume = 0f
        loopJob?.cancel()
        loopJob = scope.launch {
            delay(LOOP_GAP_MS)
            // Focus loss or disconnected headphones must not restart playback.
            if (wantsPlaying && player.playWhenReady) startPlayback()
        }
    }

    private fun fadeTo(target: Float) {
        fadeJob?.cancel()
        fadeJob = scope.launch { fadeStep(target) }
    }

    private suspend fun fadeStep(target: Float) {
        val start = player.volume
        val steps = (FADE_DURATION_MS / FADE_STEP_MS).toInt()
        repeat(steps) { i ->
            player.volume = start + (target - start) * (i + 1) / steps
            delay(FADE_STEP_MS)
        }
    }

    private companion object {
        const val MAX_VOLUME = 0.7f
        const val FADE_DURATION_MS = 1_000L
        const val LOOP_GAP_MS = 5_000L
        const val FADE_STEP_MS = 50L
    }
}
