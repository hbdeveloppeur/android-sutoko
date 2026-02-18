package friendzone3.purpletear.fr.friendzon3.custom

import android.app.Activity
import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

class SimpleSound {
    private var player : MediaPlayer? = null
    private var currentSoundName = ""
    private var currentMsPosition : Int = 0

    /**
     * Prepares and plays the sound given its name
     * @param activity : Activity
     * @param name : String
     * @param isLooping : Boolean
     * @param at : Int
     */
    fun prepareAndPlay(activity : Activity, name : String, isLooping : Boolean, at : Int = 0) {
        if(name == currentSoundName) {
            return
        }
        stop(true)

        try {
            val fd = activity.assets.openFd("sound/$name.mp3")

            player = MediaPlayer()
            currentSoundName = name
            currentMsPosition = 0
            player!!.isLooping = isLooping
            player!!.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            player!!.prepare()
            player!!.setOnPreparedListener {
                play(at)
            }
            player!!.setOnCompletionListener {
                if (!isLooping) {
                    stop(true)
                }
            }

        } catch (e: IOException) {
            currentSoundName = ""
            return
        }
    }

    /**
     * Plays the media player
     * @param at : Int
     */
    fun play(at : Int = 0) {
        if(currentSoundName == "") {
            return
        } else if(player != null && player!!.isPlaying) {
            return
        }
        player!!.seekTo(at)
        player!!.start()
    }

    /**
     * Resumes the MediaPlayer
     */
    fun resume() {
        if(player != null && player!!.isPlaying) {
            return
        }
        play(currentMsPosition)
    }

    /**
     * Pauses the MediaPlayer
     */
    fun pause() {
        try {
            if (player != null && player!!.isPlaying) {
                player!!.pause()
                currentMsPosition = player!!.currentPosition
            }
        } catch (e: Exception) {
            Log.e("Console", "Cannot pause the player.")
        }
    }


    /**
     * Stops and clears the MediaPlayer
     */
    fun stop(releaseIfNeeded : Boolean = false) {
        try {
            if (player != null && player!!.isPlaying) {
                player!!.stop()
                player!!.reset()
            }

            if(player != null && releaseIfNeeded) {
                player!!.release()
                player = null
            }

        } catch (e: Exception) {
            Log.e("Console", "Cannot stop the player.")
        }
        currentSoundName = ""
        currentMsPosition = 0
    }

    /**
     * Clears the MediaPlayer
     */
    fun onDestroy() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }
}