package fr.purpletear.friendzone.activities.textcinematic

import android.app.Activity
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sharedelements.tables.trophies.TableOfCollectedTrophies
import fr.purpletear.friendzone.Data
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.Phrase
import fr.purpletear.friendzone.tables.TableOfLinks
import fr.purpletear.friendzone.tables.TableOfPhrases
import purpletear.fr.purpleteartools.SimpleSound
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
import java.lang.IllegalStateException

class TextCinematicModel(a : Activity) {
    private var isFirstStart : Boolean = true
    val sound : SimpleSound = SimpleSound(Data.assetsDirectoryName)
    private var phrases : TableOfPhrases = TableOfPhrases()
    private var links : TableOfLinks
    private var isFirstTimeLaunchingSound : Boolean = true

    var requestManager : RequestManager = Glide.with(a)
    private set

    var currentPhrase : Phrase

    var collectedTrophies : TableOfCollectedTrophies = TableOfCollectedTrophies()

    val symbols = a.intent.getParcelableExtra<TableOfSymbols>("symbols") ?: throw IllegalStateException("Symbols not found")

    init {
        collectedTrophies.read(a)
        phrases.read(a, symbols.chapterCode)
        links = TableOfLinks()
        links.read(a, symbols.chapterCode)
        currentPhrase = getFirstPhrase()
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
    fun getImageId(context : Context, name : String) : Int {
        return Std.getResourceIdFromName(context, name, "drawable", R.drawable.zoelife)
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