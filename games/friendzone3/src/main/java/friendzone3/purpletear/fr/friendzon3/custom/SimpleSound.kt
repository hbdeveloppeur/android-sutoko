package friendzone3.purpletear.fr.friendzon3.custom

import android.app.Activity
import android.media.MediaPlayer
import friendzone3.purpletear.fr.friendzon3.Data
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

        val prepared = MediaPlayer()
        try {
            Data.setSoundSource(activity, prepared, name)
            prepared.isLooping = isLooping
            prepared.setOnCompletionListener { if (!isLooping) stop(true) }
            prepared.prepare()
            player = prepared
            currentSoundName = name
            currentMsPosition = 0
            play(at)
        } catch (e: IOException) {
            prepared.release()
            player = null
            currentSoundName = ""
            Log.e("Friendzoned3Audio", "Cannot load sound $name", e)
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