package fr.purpletear.friendzone2.activities.phone.sound

import android.app.Activity
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.Data
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.SoundHandler

class SoundScreenModel(activity : Activity, requestManager: RequestManager) {
    var requestManager: RequestManager = requestManager
        private set
    private val sh: SoundHandler = SoundHandler(Data.assetRootDir)
    var currentSoundState: SoundState = SoundState.PAUSED

    enum class SoundState {
        PAUSED,
        PLAYING
    }

    private val soundName : String = OnlineAssetsManager.getSoundFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "laze")

    /**
     * Plays the sound
     * @param activity : Activity
     */
    private fun playSound(activity: Activity) {
        sh.generateFromExternalStorage(soundName, activity, false)
        sh.play(soundName)
        currentSoundState = SoundState.PLAYING
    }

    /**
     * Stops the sound
     */
    fun stopSound() {
        sh.pause(soundName)
        currentSoundState = SoundScreenModel.SoundState.PAUSED
    }

    /**
     * Updates the sound state
     * @param activity : Activity
     */
    fun updateSound(activity: Activity) {
        when (currentSoundState) {
            SoundState.PAUSED -> {
                playSound(activity)
            }
            SoundState.PLAYING -> {
                stopSound()
            }
        }
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true

    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }
}
