/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.main

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SutokoSharedElementsData
import com.example.sharedelements.tables.trophies.TableOfCollectedTrophies
import fr.purpletear.friendzone2.Data
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.tables.TableOfLinks
import fr.purpletear.friendzone2.tables.TableOfPhrases
import fr.purpletear.friendzone2.configs.*
import purpletear.fr.purpleteartools.*

class MainModel(activity: Activity, var symbols: TableOfSymbols, rm: RequestManager, adapter: GameConversationAdapter) {
    var requestManager: RequestManager = rm
        private set
    var adapter: GameConversationAdapter = adapter
        private set
    var phrases: TableOfPhrases = TableOfPhrases()
        private set
    var links: TableOfLinks = TableOfLinks()
        private set
    var currentPhrase: Phrase
    var currentGameState: GameState = GameState.DESCRIPTION
    val mh: MemoryHandler = MemoryHandler()
    val isNoSeen: Boolean = !ChapterDetailsHandler.getChapter(activity, symbols.chapterCode, symbols).isConversation
    val sh: SoundHandler = SoundHandler(Data.assetRootDir)
    val singlePlayer = SinglePlayer(Data.assetRootDir)
    val choiceRequestCode = 58756
    val collectedTrophies : TableOfCollectedTrophies = TableOfCollectedTrophies()

    /**
     * Boolean determines if the Activity has a backgroundMedia
     */
    var hasBackgroundMedia: Boolean = false

    enum class GameState {
        DESCRIPTION,
        PAUSED,
        USER_PAUSED,
        PLAYING,
        WAITING_FOR_USER
    }

    enum class TorchState {
        OFF,
        ON,
        DISABLED
    }

    enum class GunState {
        AVAILABLE,
        DISABLED
    }

    enum class PauseButtonState {
        PAUSED,
        PLAYING
    }

    init {
        Std.debug("[INIT] MainModel")
        phrases.read(activity, symbols.chapterCode)
        links.read(activity, symbols.chapterCode)
        collectedTrophies.read(activity)

        val id = links.getDest(SutokoSharedElementsData.STARTING_PHRASE_ID)[0]
        currentPhrase = phrases.getPhrase(id)
    }


    /**
     * Plays a sound given its name
     * The file has to be in the assets folder
     *
     * @param a    Activity
     * @param name String
     * @param loop boolean
     * @see SoundHandler
     */
    fun playSound(a: Activity, name: String, loop: Boolean) {
        sh.generateFromExternalStorage(name, a, loop)
        sh.play(name)
    }

    /**
     * Returns the current chapter's title
     * @param c    Context
     * @return String
     */
    fun getChapterTitle(c : Context): String {
        val number = symbols.chapterNumber
        val title = ChapterDetailsHandler.getChapter(c, symbols.chapterCode, symbols).getTitle(c)
        return c.getString(R.string.chapter_title, number, title)
    }

    /**
     * Returns the current chapter's content
     * @return String
     */
    fun getChapterContent(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode, symbols).getDescription(c)
    }

    /**
     * Returns the conversation's name
     * @return String
     */
    fun getChapterConversationName(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode, symbols).getStartingConversationName(c)
    }

    /**
     * Returns the conversation's status
     * @return String
     */
    fun getChapterConversationStatus(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode, symbols).getStartingConversationStatus(c)
    }

    /**
     * Returns the chapter's starting profil picture
     * @return Int
     */
    fun getChapterStartingProfilPicture(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode, symbols).image
    }

    /**
     * Registers a listener
     */
    fun registerListener(a: Activity, id: Int, f: () -> Unit) {
        Finger.defineOnTouch(
                a.findViewById(id),
                a,
                f
        )
    }

    /**
     * Determines if the Phrase has an answer
     * @param p : Phrase
     * @return Boolean
     */
    fun hasAnswer(p: Phrase): Boolean {
        return links.getDestPhrases(p.id, phrases).size > 0
    }

    /**
     * Returns a phrase's answer
     * @param p : Phrase
     * @return Phrase
     */
    fun getAnswer(p: Phrase): Phrase {
        return phrases.getPhrase(links.getDest(p.id)[0])
    }

    /**
     * Returns the answers
     * @param p : Phrase
     * @return ArrayList<Phrase>
     */
    fun getChoices(p: Phrase): ArrayList<Phrase> {
        return links.getDestPhrases(p.id, phrases)
    }

    /**
     * Determines if the answer is a USER_CHOICE
     * @param srcId : Int
     * @return Boolean
     */
    fun isUserChoice(srcId: Int): Boolean {
        for (phrase in links.getDestPhrases(srcId, phrases)) {
            if(phrase.id_author == 0) {
                return true
            }
        }
        return false
    }


    /**
     * Sets the last item as "seen"
     * @param type : Phrase.Type
     */
    fun setLastSeenIf(type: Phrase.Type) {
        if(adapter.itemCount == 0) {
            return
        }
        val last = adapter.getLastItem()
        if (last.getType() !== type) {
            return
        }
        adapter.setLastSeen()
    }

    /**
     * Returns the RecyclerView LayoutManager
     *
     * @param a Activity
     * @return LayoutManager
     */
    fun getRecyclerViewLayoutManager(a: Activity): CustomLinearLayoutManager {
        return (a.findViewById(R.id.main_recyclerview) as RecyclerView).layoutManager as CustomLinearLayoutManager
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
     * Determines if it is the spectator's mode
     * @param p : Phrase
     * @return Boolean
     */
    fun isSpectatorMode(p : Phrase) : Boolean {
        return symbols.chapterNumber == 1 && p.id_author == 1
    }

    /**
     * Vibrates
     * @param activity : Activity
     * @param number : Int
     */
    fun vibrate(activity: Activity, number : Int) {
        if(number <= 0) {
            return
        }
        Std.vibrate(activity)

        val runnable = object : Runnable2("vibrate", 1000) {
            override fun run() {
                vibrate(activity, number - 1)
            }
        }
        mh.push(runnable)
        mh.run(runnable)
    }

    /**
     * Determines if the torch should be displayed
     * @return Boolean
     */
    fun shouldDisplayTorch() : Boolean {
        return symbols.chapterNumber >= 8
    }

    /**
     * Determines if we went through the forest in the story
     * @return true if we did
     */
    fun weAreInForest(): Boolean {
        val forest = arrayOf("8a", "8b", "8e", "8f")
        return forest.contains(symbols.chapterCode)
    }

    companion object {

        /**
         * Returns the current Game Layout
         * @return Int
         */
        fun getGameLayout(): Int {
            return R.layout.friendzone2_activity_main
        }
    }


}