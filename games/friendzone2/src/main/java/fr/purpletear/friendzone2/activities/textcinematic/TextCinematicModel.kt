package fr.purpletear.friendzone2.activities.textcinematic

import android.app.Activity
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import com.example.sutokosharedelements.OnlineAssetsManager.getImageFilePath
import com.example.sharedelements.tables.trophies.TableOfCollectedTrophies
import fr.purpletear.friendzone2.Data
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.TableOfLinks
import fr.purpletear.friendzone2.tables.TableOfPhrases
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.SoundHandler
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols

class TextCinematicModel(a : Activity, var symbols: TableOfSymbols) {
    private var isFirstStart : Boolean = true
    private var sh : SoundHandler = SoundHandler(Data.assetRootDir)
    var phrases : TableOfPhrases
    private set
    private var links : TableOfLinks
    private var isFirstTimeLaunchingSound : Boolean = true

    var requestManager : RequestManager = Glide.with(a)
    private set

    var currentPhrase : Phrase
    var collectedTrophies : TableOfCollectedTrophies = TableOfCollectedTrophies()

    init {
        collectedTrophies.read(a)
        sh.generateFromExternalStorage(OnlineAssetsManager.getSoundFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), getSoundName()), a, false)
        phrases = TableOfPhrases()
        phrases.read(a, filename())
        links = TableOfLinks()
        links.read(a, filename())
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
    fun getImageId(context : Context, name : String) : String {
        return OnlineAssetsManager.getImageFilePath(
            context,
            GlobalData.Game.FRIENDZONE2.id.toString(),
            name
        )
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
    fun startSound(activity: Activity) {
        if(isFirstTimeLaunchingSound) {
            isFirstTimeLaunchingSound = false
            sh.play(
                OnlineAssetsManager.getSoundFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), getSoundName())
                , getSoundPosition())
            return
        }
        sh.play(OnlineAssetsManager.getSoundFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), getSoundName()))
    }

    /**
     * Pauses the sound
     */
    fun pauseSound(activity: Activity) {
        sh.pause(OnlineAssetsManager.getSoundFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), getSoundName()))
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

    /**
     * Returns the file name
     * @return String code
     */
    private fun filename(): String {
        when (symbols.chapterCode) {
            "1a" -> return "1c"
            "5a" -> return "5c"
            "5b" -> return "5c"
        }
        return symbols.chapterCode
    }

    /**
     * Determines if the chapter is the last one
     * @return Boolean
     */
    fun isEnd(): Boolean {
        return arrayOf("9f", "10a", "10b", "10c").contains(symbols.chapterCode)
    }
}