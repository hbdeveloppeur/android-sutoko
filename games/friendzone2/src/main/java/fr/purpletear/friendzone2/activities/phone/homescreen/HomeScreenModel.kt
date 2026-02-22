package fr.purpletear.friendzone2.activities.phone.homescreen

import android.app.Activity
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.Data
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.MemoryHandler
import purpletear.fr.purpleteartools.SoundHandler
import purpletear.fr.purpleteartools.TableOfSymbols

class HomeScreenModel(requestManager: RequestManager, var symbols: TableOfSymbols) {
    var requestManager: RequestManager = requestManager
        private set
    var signalActivated: Boolean = false
    val mh : MemoryHandler = MemoryHandler()
    private var sh : SoundHandler = SoundHandler(Data.assetRootDir)
    enum class Sound {
        WRONG,
        NOTIFICATION
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

    /**
     * Plays a sound given its enum class
     * @param sound : Sound
     */
    fun playSound(activity : Activity, sound : Sound) {
        sh.generateFromExternalStorage(OnlineAssetsManager.getSoundFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), getSoundName(sound)), activity, false).play(getSoundName(sound))
    }

    /**
     * Returns the sound name given its enum class
     * @param sound  : Sound
     * @return String
     */
    private fun getSoundName(sound : Sound) : String {
        return when (sound) {
            Sound.NOTIFICATION -> "right"
            Sound.WRONG -> "wrong"
        }
    }
}
