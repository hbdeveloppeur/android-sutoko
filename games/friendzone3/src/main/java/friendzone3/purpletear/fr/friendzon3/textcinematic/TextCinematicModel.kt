package friendzone3.purpletear.fr.friendzon3.textcinematic

import android.app.Activity
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import friendzone3.purpletear.fr.friendzon3.custom.Phrase
import friendzone3.purpletear.fr.friendzon3.custom.SimpleSound
import friendzone3.purpletear.fr.friendzon3.tables.TableOfLinks
import friendzone3.purpletear.fr.friendzon3.tables.TableOfPhrases
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

class TextCinematicModel(a : Activity) {
    private var isFirstStart : Boolean = true
    val sound : SimpleSound = SimpleSound()
    private var phrases : TableOfPhrases = TableOfPhrases()
    private var links : TableOfLinks
    private var isFirstTimeLaunchingSound : Boolean = true
    var symbols : TableOfSymbols = a.intent.getParcelableExtra("symbols") ?: TableOfSymbols(GlobalData.Game.FRIENDZONE3.id)
    private set
    var requestManager : RequestManager = Glide.with(a)
    private set
    var currentPhrase : Phrase

    init {
        phrases.read(a, determineCinematicCode(symbols.chapterCode), symbols.getStoryVersion(GlobalData.Game.FRIENDZONE3.id) ?: "0.0.7")
        links = TableOfLinks()
        links.read(a, determineCinematicCode(symbols.chapterCode), symbols.getStoryVersion(GlobalData.Game.FRIENDZONE3.id) ?: "0.0.7")
        currentPhrase = getFirstPhrase()
    }

    private fun determineCinematicCode(chapterCode : String) : String {
        return when(chapterCode) {
            "1a" -> "0a"
            else -> chapterCode
        }
    }

    /**
     * Returns the first phrase
     * @return Phrase
     */
    private fun getFirstPhrase() : Phrase {
        val initial = links.getDest(0)[0]
        return phrases.getPhrase(initial)
    }

    /**
     * Returns the next phrase
     * @param phrase : Phrase - SRC
     * @return Phrase - DEST
     */
    fun getNextPhrase(phrase : Phrase) : Phrase {
        val destId = links.getDest(phrase.id)[0]
        return phrases.getPhrase(destId)
    }

    /**
     * Determines if the given phrase has a next one
     * @param phrase : Phrase
     * @return Boolean
     */
    fun hasNextPhrase(phrase : Phrase) : Boolean {
        return links.hasAnswer(phrase.id)
    }

    /**
     * Returns the image's id given its name
     * @param context : Context
     * @param name : String
     * @return Int - Image's id
     */
    fun getImageId(context : Context, name : String) : String {
        return OnlineAssetsManager.getImageFilePath(context, GlobalData.Game.FRIENDZONE3.id.toString(), name)
    }

    /**
     * Returns the resource's id from its name
     * @param c Context
     * @param name String
     * @param type String
     * @param defaultId the default id, -1 if you want to throw IllegalArgumentException else
     * @return int
     */
    private fun getResourceIdFromName(c: Context, name: String, type: String, defaultId: Int): Int {
        val id = c.resources.getIdentifier(name, type, c.packageName)
        if (id == 0) {
            if (defaultId != -1) {
                return defaultId
            }
            throw IllegalArgumentException("$type resource name $name not found")
        }
        return id
    }

    /**
     * Determines if the Activity is in first start
     * @return Boolean
     */
    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }


    /**
     * Starts the sound
     */
    fun startSound(activity : Activity) {
        if(isFirstTimeLaunchingSound) {
            isFirstTimeLaunchingSound = false
            sound.prepareAndPlay(activity, getSoundName(), false, getSoundPosition())
            return
        }
        sound.resume()
    }

    /**
     * Pauses the sound
     */
    fun pauseSound() {
        sound.pause()
    }

    /**
     * Returns the sound name
     * @return String
     */
    private fun getSoundName() : String {
        return "bg"
    }

    /**
     * Get sound position
     *
     * @return Int
     */
    private fun getSoundPosition() : Int {
        return when (symbols.chapterCode) {
            "9b" -> 24520
            "9d" -> 24520
            "10a" -> 24520
            else -> 0
        }
    }

    enum class LogEvent {
        SEEN_END,
        PRESSED_RATE_BUTTON
    }
}