package com.example.sharedelements.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.annotation.Keep

@Keep
interface CVideoViewListener {
    fun onVideoStarted()
    fun onVideoDetached()
    fun onRequestError()
}

@Keep
class CVideoView : TextureView, TextureView.SurfaceTextureListener {
    private var activity: Activity? = null
    private var isLooping: Boolean = false
    private var player: MediaPlayer? = null
    private lateinit var callback: CVideoViewListener
    var source: Uri? = null
        private set
    private var playOnSurfaceTextureAvailable: Boolean = false


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        this.surfaceTextureListener = this
    }


    private fun isFree(): Boolean {
        return this.activity != null
                && !this.activity!!.isFinishing
                && this.source != null
                && surfaceTexture != null
    }

    fun pause() {
        if (this.player != null && this.player!!.isPlaying) {
            this.player!!.pause()
        }
    }

    fun play() {
        if (surfaceTexture == null) {
            playOnSurfaceTextureAvailable = true
            return
        }
        if (player != null) {
            player!!.start()
            // this.callback.onVideoStarted()
        } else {
            startNewMediaPlayer()
        }
    }

    fun isPlaying(): Boolean {
        return this.player != null && this.player!!.isPlaying
    }

    fun setVideo(activity: Activity?, url: String, isLooping: Boolean = true) {
        playOnSurfaceTextureAvailable = false
        this.activity = activity
        this.source = Uri.parse(url)
        this.isLooping = isLooping
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (playOnSurfaceTextureAvailable) {
            playOnSurfaceTextureAvailable = false
            this.play()
        }
    }


    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Nothing to do
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    @SuppressLint("Recycle", "ObsoleteSdkInt")
    private fun startNewMediaPlayer() {
        if (!isFree()) {
            Log.e("CVideoView", "Not free for source ${source.toString()}")
            return
        }
        val surface = Surface(surfaceTexture)

        try {
            this.player = MediaPlayer()
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
                this.player!!.setOnPreparedListener setOnPreparedListener@{
                    this.callback.onVideoStarted()
                }
                this.player!!.setOnErrorListener { mediaPlayer, i, i2 ->

                    this.callback.onRequestError()
                    true
                }
            } else {
                this.player!!.setOnErrorListener { mediaPlayer, i, i2 ->

                    this.callback.onRequestError()
                    true
                }
                this.player!!.setOnInfoListener setOnInfoListener@{ _, what, _ ->
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        this.callback.onVideoStarted()
                    }
                    val array = arrayOf(
                        MediaPlayer.MEDIA_INFO_UNKNOWN,
                        MediaPlayer.MEDIA_ERROR_UNSUPPORTED,
                        MediaPlayer.MEDIA_ERROR_UNKNOWN,
                        MediaPlayer.MEDIA_ERROR_TIMED_OUT,
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED,
                        MediaPlayer.MEDIA_ERROR_MALFORMED,
                        MediaPlayer.MEDIA_ERROR_IO,
                        MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK,
                        MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING,
                        MediaPlayer.MEDIA_INFO_NOT_SEEKABLE
                    )
                    if (array.contains(what)) {
                        this.callback.onRequestError()
                    }
                    false
                }
            }

            var ht: HandlerThread? = HandlerThread(
                "video player thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE
            )
            ht!!.start()
            val handler = Handler(ht.looper)
            handler.post {
                try {

                    player!!.setVolume(0f, 0f)
                    player!!.isLooping = this.isLooping
                    player!!.setDataSource(this.activity!!, this.source!!)
                    player!!.setSurface(surface)
                    player!!.setOnPreparedListener {
                        player!!.start()
                    }
                    player!!.prepareAsync()
                } catch (e: Exception) {
                    Log.e("CVideoView", "Error while preparing video", e)
                } finally {
                    surface.release()
                    if (ht != null) {
                        ht!!.quit()
                        ht = null
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun free(surface: SurfaceTexture? = surfaceTexture) {
        if (this.player != null) {
            player!!.stop()
            player!!.reset()
            player!!.release()
            player = null
        } else {
            Log.e("CVideoView", "Cannot free player because it's null")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                surface?.release()
            } catch (e: Exception) {
                Log.e("CVideoView", "Error while releasing surface texture", e)
            }
        }
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    /**
     * Init vars
     * @param isLooping Boolean
     * @param callback CVideoViewListener
     */
    fun initialize(isLooping: Boolean, callback: CVideoViewListener) {
        this.isLooping = isLooping
        this.callback = callback
    }
}