package purpletear.fr.purpleteartools

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util


/**
 * As soon as a build you use this, enable JAVA8 in build gradle file
 * targetCompatibility JavaVersion.VERSION_1_8
 */
class PurpleExoPlayer(
    private val appNameResId: Int,
    var url: String,
    private var isLooping: Boolean
) {
    private var playWhenReady: Boolean = false
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private var player: SimpleExoPlayer? = null
    var isReady: Boolean = false
        private set
    private var listener: Player.Listener? = null

    init {
        check(Looper.myLooper() == Looper.getMainLooper())
        player = null
    }


    fun onResume(context: Context, playerView: PlayerView? = null): Boolean {
        if ((Util.SDK_INT < 24 && player == null)) {
            create(context, playerView)
            return true
        }
        return false
    }

    fun onStart(context: Context, playerView: PlayerView? = null): Boolean {
        if (Util.SDK_INT >= 24) {
            create(context, playerView)
            return true
        }
        return false
    }

    fun onPause() {
        if (Util.SDK_INT < 24) {
            release()
        }
    }

    fun onStop() {
        if (Util.SDK_INT >= 24) {
            release()
        }
    }

    fun create(context: Context, playerView: PlayerView? = null) {
        check(Looper.myLooper() == Looper.getMainLooper())
        check(player == null)

        player = SimpleExoPlayer.Builder(context).build()

        // This is the MediaSource representing the media to be played.
        val mediaItem = MediaItem.fromUri(Uri.parse(url))


        playerView?.player = player

        player!!.setMediaItem(mediaItem)
        player!!.playWhenReady = playWhenReady

        player!!.seekTo(currentWindow, playbackPosition)
        player!!.prepare()
        player!!.repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF

        this.listener = object : Player.Listener {


            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                when (state) {
                    ExoPlayer.STATE_READY -> {
                        isReady = true
                    }
                    else -> {}
                }

            }

        }
        player!!.addListener(this.listener!!)
    }

    fun setPlayWhenReady(value: Boolean) {
        if (player == null) {
            return
        }
        player!!.playWhenReady = value
    }

    fun isPlaying(): Boolean {
        return player != null && player!!.isPlaying
    }

    fun setVolume(f: Float) {
        if (player != null) {
            player!!.volume = f
        }
    }

    fun volume(): Float {
        if (player != null) {
            return player!!.volume
        }
        return 0f
    }

    fun play(onIsPlayingChanged: (isPlaying: Boolean) -> Unit, onEnded: () -> Unit) {

        if (player != null && !player!!.isPlaying) {
            if (this.listener != null) {
                player!!.removeListener(this.listener!!)
            }
            this.listener = object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    Handler(Looper.getMainLooper()).post {
                        onIsPlayingChanged(isPlaying)
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)

                    when (state) {
                        ExoPlayer.STATE_READY -> {
                            isReady = true
                        }
                        ExoPlayer.STATE_ENDED -> {
                            if (!isLooping && player != null) {
                                player!!.playWhenReady = false
                                onEnded()
                            }
                        }
                        else -> {}
                    }

                }
            }
            player!!.addListener(this.listener!!)
            player!!.playWhenReady = true
            return
        }
    }

    fun getDuration(): Long {
        if (player == null) {
            return 0
        }
        return player!!.duration
    }

    fun getPosition(): Long {
        if (player == null) {
            return 0
        }
        return player!!.currentPosition
    }

    fun rollback() {
        if (player != null) {
            playbackPosition = 0
            player!!.seekTo(0)
            playWhenReady = false
        }
    }

    fun release() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }
}