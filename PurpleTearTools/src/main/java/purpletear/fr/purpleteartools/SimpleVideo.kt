package purpletear.fr.purpleteartools

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import java.io.File
import java.io.FileInputStream

/**
 * SimpleVideo start, play and pauses a VideoView
 * @author Hocine Belbouab <hbdeveloppeur@gmail.com>
 */
object SimpleVideo {

    /**
     * Starts a video in a VideoView
     * @param activity : Activity
     * @param videoViewId : Int
     * @param isLooping : Boolean (default value = false)
     */
    fun start(activity: Activity, videoViewId: Int, videoFile: File, isLooping: Boolean = false) {
        activity.runOnUiThread {
            videoFile.setExecutable(true)
            val v = activity.findViewById<VideoView>(videoViewId)
            v.setMediaController(null)

            v.setVideoPath(videoFile.absolutePath)
            v.setOnPreparedListener {
                it.isLooping = isLooping
                it.setVolume(0f, 0f)
                v.start()
            }
        }
    }

    /**
     * Starts a video in a VideoView
     * @param activity : Activity
     * @param videoViewId : Int
     * @param isLooping : Boolean (default value = false)
     */
    fun start(activity: Activity, videoViewId: Int, videoFile: File, onLoaded : () -> Unit, isLooping: Boolean = false) {
        activity.runOnUiThread {
            videoFile.setExecutable(true)
            val v = activity.findViewById<VideoView>(videoViewId)
            v.setMediaController(null)

            v.setVideoPath(videoFile.absolutePath)
            v.setOnPreparedListener {
                it.isLooping = isLooping
                it.setVolume(0f, 0f)
                v.start()
                Handler(Looper.getMainLooper()).post(onLoaded)
            }
        }
    }

    /**
     * Resumes a VideoView
     * @param activity: Activity
     * @param videoViewId : Int
     */
    fun play(activity: Activity, videoViewId: Int) {
        val v = activity.findViewById<VideoView>(videoViewId)
        if (!v.isPlaying) {
            v.start()
        }
    }

    /**
     * Pauses a VideoView
     * @param activity: Activity
     * @param videoViewId : Int
     */
    fun pause(activity: Activity, videoViewId: Int) {
        val v = activity.findViewById<VideoView>(videoViewId)
        if (v.isPlaying && v.canPause()) {
            v.pause()
        }
    }
}