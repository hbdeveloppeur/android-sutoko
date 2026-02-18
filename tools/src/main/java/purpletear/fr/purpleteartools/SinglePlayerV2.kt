@file:Suppress("JoinDeclarationAndAssignment")

package purpletear.fr.purpleteartools

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

class SinglePlayerV2 {
    private var mediaPlayer: MediaPlayer?
    private var currentUrl: String
    private var currentState : State

    enum class State {
        LOADING,
        STARTED,
        PAUSED,
        STOPPED
    }

    init {
        currentState = State.STOPPED
        mediaPlayer = null
        currentUrl = ""
    }
    fun isPlaying(url : String) : Boolean {
        return currentState == State.STARTED && (mediaPlayer?.isPlaying ?: false) && currentUrl == url
    }

    fun start(url: String, onStarted: () -> Unit, onFinish: () -> Unit) {
        start(url, 0, onStarted, onFinish)
    }

    fun start(url : String, fromMs : Int, onStarted : () -> Unit, onFinish: () -> Unit) {
        if(mediaPlayer != null && mediaPlayer!!.isPlaying) {
            return
        }

        currentState = State.LOADING
        currentUrl = url
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
        }
        mediaPlayer!!.setOnPreparedListener {
            player ->
            currentState = State.STARTED
            player.start()
            Handler(Looper.getMainLooper()).post(onStarted)
        }
        mediaPlayer!!.setOnCompletionListener {
            Handler(Looper.getMainLooper()).post(onFinish)
            release()
        }
        mediaPlayer!!.setOnErrorListener { _, what, extra ->
            Log.e("SinglePlayerV2", "Encountered error $what with extra $extra")
            true
        }
        mediaPlayer!!.setOnBufferingUpdateListener { _, percent ->
            Log.i("SinglePlayerV2", "Buffering : $percent%")
        }
    }

    fun isLoading(url : String) : Boolean {
        return currentState == State.LOADING && url == url
    }

    fun pause() {
        if(mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            currentState = State.PAUSED
        }
    }

    fun stop() {
        if(mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            currentState = State.STOPPED
            currentUrl = ""
        }
        if(mediaPlayer != null) {
            release()
        }
    }

    fun onActivityPaused() {
        release()
    }

    private fun release() {
        currentUrl = ""
        mediaPlayer?.release()
        mediaPlayer = null
        currentState = State.STOPPED
    }
}