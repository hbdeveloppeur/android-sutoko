package com.purpletear.game.presentation.game_preview.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Plays the story's menu ambience on the GamePreview screen while [muted] is
 * false and the screen is resumed. Fades in over 1s up to 70% volume, loops
 * with a 5s pause between loops, and fades out over 1s when paused, muted or
 * leaving the screen.
 */
@Composable
fun GamePreviewMenuSoundEffect(
    soundUrl: String?,
    muted: Boolean,
) {
    if (soundUrl.isNullOrBlank()) return

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow
        .collectAsStateWithLifecycle()

    val player = remember(soundUrl) {
        GamePreviewMenuSoundPlayer(soundUrl)
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    LaunchedEffect(player, muted, lifecycleState) {
        player.setPlaying(!muted && lifecycleState == Lifecycle.State.RESUMED)
    }
}

/**
 * Small MediaPlayer wrapper for the preview menu ambience. All public calls
 * are main-thread; callbacks arrive on the main thread because the player is
 * created on it. [release] is terminal and idempotent.
 */
private class GamePreviewMenuSoundPlayer(
    soundUrl: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player = MediaPlayer()
    private var fadeJob: Job? = null
    private var loopJob: Job? = null
    private var volume = 0f
    private var released = false
    private var prepared = false
    private var wantsPlaying = false

    init {
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player.setOnPreparedListener {
            prepared = true
            if (wantsPlaying) startPlayback()
        }
        player.setOnCompletionListener { scheduleNextLoop() }
        player.setOnErrorListener { _, _, _ ->
            release()
            true
        }
        runCatching {
            player.setDataSource(soundUrl)
            player.prepareAsync()
        }.onFailure { release() }
    }

    /** Idempotent: only state transitions trigger fades. */
    fun setPlaying(playing: Boolean) {
        if (released || wantsPlaying == playing) return
        wantsPlaying = playing
        if (!prepared) return
        if (playing) startPlayback() else stopPlayback()
    }

    fun release() {
        if (released) return
        released = true
        scope.cancel()
        runCatching { player.release() }
    }

    private fun startPlayback() {
        loopJob?.cancel()
        runCatching { if (!player.isPlaying) player.start() }
        fadeTo(MAX_VOLUME)
    }

    private fun stopPlayback() {
        loopJob?.cancel()
        fadeJob?.cancel()
        fadeJob = scope.launch {
            fadeStep(0f)
            runCatching { if (player.isPlaying) player.pause() }
        }
    }

    private fun scheduleNextLoop() {
        volume = 0f
        applyVolume()
        loopJob = scope.launch {
            delay(LOOP_GAP_MS)
            if (released || !wantsPlaying) return@launch
            startPlayback()
        }
    }

    private fun fadeTo(target: Float) {
        fadeJob?.cancel()
        fadeJob = scope.launch { fadeStep(target) }
    }

    private suspend fun fadeStep(target: Float) {
        val start = volume
        val steps = (FADE_DURATION_MS / FADE_STEP_MS).toInt().coerceAtLeast(1)
        repeat(steps) { i ->
            volume = start + (target - start) * (i + 1) / steps
            applyVolume()
            delay(FADE_STEP_MS)
        }
    }

    private fun applyVolume() {
        if (released) return
        runCatching { player.setVolume(volume, volume) }
    }

    private companion object {
        const val MAX_VOLUME = 0.7f
        const val FADE_DURATION_MS = 1_000L
        const val LOOP_GAP_MS = 5_000L
        const val FADE_STEP_MS = 50L
    }
}
