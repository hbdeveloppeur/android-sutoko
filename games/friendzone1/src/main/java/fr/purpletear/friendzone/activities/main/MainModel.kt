/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SutokoSharedElementsData
import com.example.sharedelements.tables.trophies.TableOfCollectedTrophies
import fr.purpletear.friendzone.Data
import fr.purpletear.friendzone.R
import fr.purpletear.friendzone.config.*
import fr.purpletear.friendzone.tables.TableOfLinks
import fr.purpletear.friendzone.tables.TableOfPhrases
import purpletear.fr.purpleteartools.*

class MainModel(activity: Activity, var symbols: TableOfSymbols, rm: RequestManager, adapter: GameConversationAdapter) {
    private var isFirstStart = true

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
    val isNoSeen: Boolean = !ChapterDetailsHandler.getChapter(activity, symbols.chapterCode).isConversation
    val sound : SimpleSound = SimpleSound("friendzone1_assets")
    val sound2 : SimpleSound = SimpleSound("friendzone1_assets")
    val collectedTrophies : TableOfCollectedTrophies = TableOfCollectedTrophies()

    enum class GameType {
        NORMAL, SMS
    }

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

    init {
        Std.debug("[INIT] MainModel")
        phrases.read(activity, symbols.chapterCode)
        links.read(activity, symbols.chapterCode)
        collectedTrophies.read(activity)

        val id = links.getDest(SutokoSharedElementsData.STARTING_PHRASE_ID)[0]
        currentPhrase = phrases.getPhrase(id)
    }

    enum class SoundType {
        FOREGROUND,
        BACKGROUND
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
    fun playSound(a: Activity, name: String, loop: Boolean, type : SoundType) {
        when (type) {
            SoundType.FOREGROUND -> {
                sound.prepareAndPlay(a, name, loop)
            }

            SoundType.BACKGROUND -> {
                sound2.prepareAndPlay(a, name, loop)
            }
        }
    }

    /**
     * Returns the current chapter's title
     * @param c    Context
     * @return String
     */
    fun getChapterTitle(c : Context): String {
        val number = symbols.chapterNumber
        val title = ChapterDetailsHandler.getChapter(c, symbols.chapterCode).getTitle(c)
        return c.getString(R.string.chapter_title, number, title)
    }

    /**
     * Returns the current chapter's content
     * @return String
     */
    fun getChapterContent(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode).getDescription(c)
    }

    /**
     * Returns the conversation's name
     * @return String
     */
    fun getChapterConversationName(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode).getStartingConversationName(c)
    }

    /**
     * Returns the conversation's status
     * @return String
     */
    fun getChapterConversationStatus(c : Context): String {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode).getStartingConversationStatus(c)
    }

    /**
     * Returns the chapter's starting profil picture
     * @return Int
     */
    fun getChapterStartingProfilPicture(c : Context): Int {
        return ChapterDetailsHandler.getChapter(c, symbols.chapterCode).image
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
        return links.getDest(srcId).size > 1
    }


    /**
     * Sets the last item as "seen"
     * @param type : Phrase.Type
     */
    fun setLastSeenIf(type: Phrase.Type) {
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
     * @see android.support.v7.widget.RecyclerView.LayoutManager
     */
    fun getRecyclerViewLayoutManager(a: Activity): CustomLinearLayoutManager {
        return (a.findViewById(R.id.main_recyclerview) as androidx.recyclerview.widget.RecyclerView).layoutManager as CustomLinearLayoutManager
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }

    fun logEvent() {
        if(!Data.logEventEnabled) {
            return
        }
        val params = Bundle()
        params.putString("chapter_code", symbols.chapterCode)
        params.putString("os", "android")
    }

    companion object {
        /**
         * Returns the current Game Type
         * @param chapterCode : String
         * @return GameType
         */
        fun getGameType(chapterCode: String): GameType {
            if (chapterCode == "7a") {
                return GameType.SMS
            }
            return GameType.NORMAL
        }

        /**
         * Returns the current Game Layout
         * @param chapterCode : String
         * @return Int
         */
        fun getGameLayout(chapterCode: String): Int {
            if (getGameType(chapterCode) == GameType.SMS) {
                return R.layout.friendzone1_activity_sms_
            }
            return R.layout.friendzone_activity_main_
        }
    }


}