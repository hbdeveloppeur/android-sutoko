package fr.purpletear.sutoko.screens.splashscreen

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import fr.purpletear.sutoko.R

internal class SplashSound(activity: ComponentActivity) : DefaultLifecycleObserver {
    private val lifecycle = activity.lifecycle
    private var player: ExoPlayer? = ExoPlayer.Builder(activity).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
                .build(),
            false,
        )
        setMediaItem(MediaItem.fromUri(rawResourceUri(activity, R.raw.splash_christmas_sound)))
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) this@SplashSound.release()
            }

            override fun onPlayerError(error: PlaybackException) = this@SplashSound.release()
        })
        prepare()
    }

    init {
        lifecycle.addObserver(this)
    }

    fun play() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            player?.play()
        }
    }

    fun onVideoDisposed(finishPlaying: Boolean) {
        if (!finishPlaying || player?.playWhenReady == false) release()
    }

    override fun onPause(owner: LifecycleOwner) = release()

    private fun release() {
        val audioPlayer = player ?: return
        player = null
        lifecycle.removeObserver(this)
        audioPlayer.release()
    }
}
