package com.purpletear.game.presentation.game_play.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Snapshot of the vocal message playback, merged into the UI state by the ViewModel.
 */
data class VocalPlayback(
    val url: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
)

/**
 * Owns every MediaPlayer of a game play session: typing ticks, the ambient/looping channel,
 * overlapping one-shots, vocal messages (with progress tracking), and the visual novel
 * channels with their fade-out. Jobs run on the ViewModel's scope, so everything dies with
 * the ViewModel; [releaseSessionSounds] / [releaseAll] cover explicit teardown.
 */
class GameAudioController(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private var typingPlayer: MediaPlayer? = null

    /** Ambient/looping channel: a new looping sound replaces the previous one. */
    private var soundPlayer: MediaPlayer? = null

    /** Non-looping sounds: each one gets its own player so effects can overlap. */
    private val oneShotSoundPlayers = mutableSetOf<MediaPlayer>()

    /** Visual novel sounds: one player per authored sound, all fading out together on dismiss. */
    private val visualNovelChannels = mutableListOf<VisualNovelChannel>()
    private var visualNovelFadeJob: Job? = null

    /** Visual novel dialog sounds: one-shots fired by the overlay as each dialog appears. */
    private val visualNovelDialogPlayers = mutableSetOf<MediaPlayer>()

    private data class VisualNovelChannel(val player: MediaPlayer, val volume: Float)

    private var vocalPlayer: MediaPlayer? = null
    private var vocalProgressJob: Job? = null

    private val _vocal = MutableStateFlow(VocalPlayback())
    val vocal: StateFlow<VocalPlayback> = _vocal.asStateFlow()

    fun playTypingSound() {
        typingPlayer?.release()
        typingPlayer = MediaPlayer.create(context, R.raw.game_presentation_typing)?.apply {
            setOnCompletionListener {
                release()
                typingPlayer = null
            }
            start()
        }
    }

    fun playSound(soundUrl: String, loop: Boolean, volume: Float) {
        if (loop) {
            playLoopingSound(soundUrl, volume)
        } else {
            playOneShotSound(soundUrl, volume)
        }
    }

    private fun playLoopingSound(soundUrl: String, volume: Float) {
        soundPlayer?.release()
        soundPlayer = try {
            MediaPlayer().apply {
                setDataSource(soundUrl)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play sound: $soundUrl", e)
            null
        }
    }

    /**
     * Fire-and-forget playback: every one-shot sound owns its player, so several
     * effects can overlap each other and the ambient loop. The player removes and
     * releases itself on completion; [releaseOneShotSounds] covers early teardown.
     */
    private fun playOneShotSound(soundUrl: String, volume: Float) {
        val player = try {
            MediaPlayer().apply {
                setDataSource(soundUrl)
                setVolume(volume, volume)
                prepare()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play sound: $soundUrl", e)
            return
        }
        oneShotSoundPlayers += player
        player.setOnCompletionListener { mp ->
            oneShotSoundPlayers.remove(mp)
            mp.release()
        }
        player.start()
    }

    private fun releaseOneShotSounds() {
        oneShotSoundPlayers.forEach {
            it.setOnCompletionListener(null)
            it.release()
        }
        oneShotSoundPlayers.clear()
    }

    fun stopSound() {
        soundPlayer?.stop()
        soundPlayer?.release()
        soundPlayer = null
        releaseOneShotSounds()
    }

    fun toggleVocal(audioUrl: String) {
        val current = _vocal.value
        if (current.url == audioUrl && current.isPlaying) {
            pauseVocal()
        } else {
            playVocal(audioUrl)
        }
    }

    private fun pauseVocal() {
        vocalPlayer?.pause()
        vocalProgressJob?.cancel()
        _vocal.value = _vocal.value.copy(isPlaying = false)
    }

    fun playVocal(audioUrl: String) {
        if (audioUrl.isBlank()) {
            Log.e("GameEngine", "Cannot play vocal: audioUrl is blank")
            return
        }

        if (!File(audioUrl).exists()) {
            Log.e("GameEngine", "Cannot play vocal: file not found at $audioUrl")
        }

        vocalPlayer?.setOnCompletionListener(null)
        vocalPlayer?.release()
        vocalProgressJob?.cancel()

        vocalPlayer = try {
            MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
                setOnCompletionListener {
                    if (vocalPlayer === this) {
                        release()
                        vocalPlayer = null
                        vocalProgressJob?.cancel()
                        _vocal.value = _vocal.value.copy(isPlaying = false, progress = 1f)
                    }
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play vocal: $audioUrl", e)
            null
        }

        if (vocalPlayer != null) {
            _vocal.value = VocalPlayback(url = audioUrl, isPlaying = true, progress = 0f)
            startVocalProgressTracking()
        }
    }

    private fun startVocalProgressTracking() {
        vocalProgressJob?.cancel()
        vocalProgressJob = scope.launch {
            while (isActive) {
                val player = vocalPlayer
                val duration = player?.duration?.takeIf { it > 0 }
                val position = player?.currentPosition?.takeIf { it >= 0 }
                if (duration != null && position != null) {
                    val progress = position.toFloat() / duration.toFloat()
                    _vocal.value = _vocal.value.copy(progress = progress.coerceIn(0f, 1f))
                }
                delay(100)
            }
        }
    }

    /**
     * Every authored sound owns its player, so channels overlap freely and keep their own
     * volume/loop settings. [fadeOutVisualNovelSounds] / [releaseVisualNovelSounds] cover teardown.
     */
    fun playVisualNovelSounds(sounds: List<Node.VisualNovel.Sound>) {
        releaseVisualNovelSounds()
        sounds.forEach { sound ->
            if (sound.path.isBlank()) return@forEach
            val player = try {
                MediaPlayer().apply {
                    setDataSource(sound.path)
                    isLooping = sound.loop
                    setVolume(sound.volume, sound.volume)
                    // Async prepare keeps the UI free even though the path is a local file
                    // bundled in the story's assets/ directory.
                    setOnPreparedListener { it.start() }
                    setOnErrorListener { mp, what, extra ->
                        Log.e("GameEngine", "Failed to play visual novel sound: ${sound.path} (what=$what extra=$extra)")
                        mp.release()
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("GameEngine", "Failed to play visual novel sound: ${sound.path}", e)
                null
            } ?: return@forEach
            visualNovelChannels += VisualNovelChannel(player, sound.volume)
        }
    }

    /** Stepped volume ramp to silence, then stop/release (GamePreviewMenuSoundEffect pattern). */
    fun fadeOutVisualNovelSounds() {
        visualNovelFadeJob?.cancel()
        val channels = visualNovelChannels.toList()
        visualNovelChannels.clear()
        if (channels.isEmpty()) return
        visualNovelFadeJob = scope.launch {
            val steps = (VISUAL_NOVEL_FADE_MS / VISUAL_NOVEL_FADE_STEP_MS).toInt()
            repeat(steps) { step ->
                val scale = 1f - (step + 1).toFloat() / steps
                channels.forEach { channel ->
                    runCatching {
                        val volume = channel.volume * scale
                        channel.player.setVolume(volume, volume)
                    }
                }
                delay(VISUAL_NOVEL_FADE_STEP_MS)
            }
            channels.forEach { channel ->
                runCatching { channel.player.stop() }
                channel.player.release()
            }
        }
    }

    /**
     * Plays the one-shot sound attached to a visual novel dialog (called by the overlay when
     * the dialog appears). Fire-and-forget like [playOneShotSound]; the path is a local file
     * bundled in the story's `assets/` directory, prepared asynchronously to stay non-blocking.
     */
    fun playVisualNovelDialogSound(path: String) {
        if (path.isBlank()) return
        val player = try {
            MediaPlayer().apply {
                setDataSource(path)
                setOnPreparedListener { it.start() }
                setOnCompletionListener { mp ->
                    visualNovelDialogPlayers.remove(mp)
                    mp.release()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("GameEngine", "Failed to play visual novel dialog sound: $path (what=$what extra=$extra)")
                    visualNovelDialogPlayers.remove(mp)
                    mp.release()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("GameEngine", "Failed to play visual novel dialog sound: $path", e)
            return
        }
        visualNovelDialogPlayers += player
    }

    fun releaseVisualNovelDialogSounds() {
        visualNovelDialogPlayers.forEach { player ->
            player.setOnPreparedListener(null)
            player.setOnCompletionListener(null)
            player.setOnErrorListener(null)
            runCatching { player.stop() }
            player.release()
        }
        visualNovelDialogPlayers.clear()
    }

    private fun releaseVisualNovelSounds() {
        visualNovelFadeJob?.cancel()
        visualNovelFadeJob = null
        visualNovelChannels.forEach { channel ->
            runCatching { channel.player.stop() }
            channel.player.release()
        }
        visualNovelChannels.clear()
    }

    /**
     * Releases the sounds of a play session (typing, ambient, one-shots, vocal) and resets
     * the vocal state. Visual novel channels survive: their overlay outlives the reset.
     */
    fun releaseSessionSounds() {
        typingPlayer?.release()
        typingPlayer = null

        soundPlayer?.stop()
        soundPlayer?.release()
        soundPlayer = null
        releaseOneShotSounds()

        vocalPlayer?.setOnCompletionListener(null)
        vocalPlayer?.release()
        vocalPlayer = null
        vocalProgressJob?.cancel()
        vocalProgressJob = null
        _vocal.value = VocalPlayback()
    }

    /** Full teardown (ViewModel cleared): session sounds plus visual novel channels. */
    fun releaseAll() {
        releaseSessionSounds()
        releaseVisualNovelSounds()
        releaseVisualNovelDialogSounds()
    }

    private companion object {
        const val VISUAL_NOVEL_FADE_MS = 600L
        const val VISUAL_NOVEL_FADE_STEP_MS = 50L
    }
}
