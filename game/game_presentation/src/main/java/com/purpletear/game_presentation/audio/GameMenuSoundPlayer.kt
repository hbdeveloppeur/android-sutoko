package com.purpletear.game_presentation.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A class for playing game menu sounds with fade in/out effects.
 */
class GameMenuSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String = ""
    private var fadeJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private enum class State {
        IDLE,
        LOADING,
        PLAYING,
        FADING_IN,
        FADING_OUT,
        PAUSED
    }

    private var currentState = State.IDLE

    /**
     * Plays the menu sound with fade in effect.
     *
     * @param url The URL of the sound to play
     * @param fadeInDuration The duration of the fade in effect in milliseconds
     */
    fun playWithFadeIn(url: String, fadeInDuration: Long = 1000) {
        if (url.isEmpty()) return

        // If already playing the same URL, do nothing
        if (currentUrl == url && (currentState == State.PLAYING || currentState == State.FADING_IN)) {
            return
        }

        // If already playing a different URL, stop it first
        if (mediaPlayer != null) {
            stopWithFadeOut(0)
        }

        currentUrl = url
        currentState = State.LOADING

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                setOnPreparedListener {
                    currentState = State.FADING_IN
                    it.setVolume(0f, 0f)
                    it.start()
                    it.isLooping = true

                    // Start fade in
                    fadeIn(fadeInDuration)
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("GameMenuSoundPlayer", "Error playing sound: $what, $extra")
                    release()
                    true
                }
                prepareAsync()
            } catch (e: Exception) {
                Log.e("GameMenuSoundPlayer", "Error setting up MediaPlayer", e)
                release()
            }
        }
    }

    /**
     * Stops the menu sound with fade out effect.
     *
     * @param fadeOutDuration The duration of the fade out effect in milliseconds
     */
    fun stopWithFadeOut(fadeOutDuration: Long = 1000) {
        if (mediaPlayer == null || currentState == State.IDLE) {
            return
        }

        // If already fading out, do nothing
        if (currentState == State.FADING_OUT) {
            return
        }

        // Cancel any ongoing fade in
        fadeJob?.cancel()

        // If fade out duration is 0, stop immediately
        if (fadeOutDuration <= 0) {
            release()
            return
        }

        currentState = State.FADING_OUT
        fadeOut(fadeOutDuration)
    }

    /**
     * Releases resources when the player is no longer needed.
     */
    fun release() {
        fadeJob?.cancel()
        fadeJob = null

        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentUrl = ""
        currentState = State.IDLE
    }

    private fun fadeIn(duration: Long) {
        fadeJob?.cancel()

        fadeJob = coroutineScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val endTime = startTime + duration

                while (System.currentTimeMillis() < endTime && mediaPlayer != null) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val volume = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    try {
                        mediaPlayer?.setVolume(volume, volume)
                    } catch (t: Throwable) {
                        Log.e("GameMenuSoundPlayer", "Error during fadeIn setVolume", t)
                        break
                    }
                    delay(50) // Update volume every 50ms
                }

                // Ensure we reach full volume
                try {
                    mediaPlayer?.setVolume(1f, 1f)
                } catch (t: Throwable) {
                    Log.e("GameMenuSoundPlayer", "Error setting final volume in fadeIn", t)
                }
                currentState = State.PLAYING
            } catch (t: Throwable) {
                Log.e("GameMenuSoundPlayer", "fadeIn failed", t)
                currentState = if (mediaPlayer != null) State.PLAYING else State.IDLE
            }
        }
    }

    private fun fadeOut(duration: Long) {
        fadeJob?.cancel()

        fadeJob = coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + duration

            while (System.currentTimeMillis() < endTime && mediaPlayer != null) {
                val elapsed = System.currentTimeMillis() - startTime
                val volume = (1 - elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                mediaPlayer?.setVolume(volume, volume)
                delay(50) // Update volume every 50ms
            }

            // Release resources after fade out
            release()
        }
    }
}