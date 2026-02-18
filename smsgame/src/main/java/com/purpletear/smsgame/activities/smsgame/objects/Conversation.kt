@file:Suppress("CanBePrimaryConstructorProperty")

package com.purpletear.smsgame.activities.smsgame.objects

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.Keep
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.SutokoParams
import com.example.sutokosharedelements.SutokoSharedElementsData
import com.purpletear.smsgame.BuildConfig
import com.purpletear.smsgame.activities.manga.MangaMessage
import com.purpletear.smsgame.activities.smsgame.ConversationInterface
import com.purpletear.smsgame.activities.smsgame.adapter.GameConversationAdapter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCharacters
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCreatorResources
import com.purpletear.smsgame.activities.smsgame.tables.TableOfLinks
import com.purpletear.smsgame.activities.smsgame.tables.TableOfPhrases
import com.purpletear.sutoko.game.model.Game
import purpletear.fr.purpleteartools.Language
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
import java.io.FileNotFoundException

/**
 * A conversation understands the PurpleTear's Story Generator and fires
 * callbacks so it is easier to handle events within the story.
 * @author Hocine Belbouab <hbdeveloppeur@gmail.com>
 */
@Keep
class Conversation(
    activity: Activity,
    requestManager: RequestManager,
    val card: Game,
    storyMetadata: StoryMetadata,
    sutokoParams: SutokoParams,
    val characters: TableOfCharacters,
    val chapters: ArrayList<StoryChapter>,
    storyType: StoryType,
    symbols: TableOfSymbols
) {
    private val chapterCode: String = symbols.chapterCode
    private val storyMetadata: StoryMetadata = storyMetadata
    var isChoiceMode: Boolean = false
    var currentPhrase: Phrase? = null
    val links: TableOfLinks
    val phrases: TableOfPhrases
    var adapter: GameConversationAdapter = GameConversationAdapter(
        activity,
        ArrayList(),
        characters,
        requestManager,
        storyMetadata.storyId,
        activity as ConversationInterface,
        sutokoParams,
        storyType,
        card,
        symbols.firstName
    )
    private val callback: ConversationInterface = activity as ConversationInterface
    var timeMode: Phrase.TimeUpdateMode = Phrase.TimeUpdateMode.MANUAL
    var creatorResources: TableOfCreatorResources = TableOfCreatorResources()
    var isFinished: Boolean = false
    val lastIndex: Int
        get() {
            return adapter.getLastIndex()
        }

    enum class GameMode {
        NORMAL,
        REAL_TIME
    }

    init {
        try {
            links = TableOfLinks(
                activity,
                storyMetadata.storyId,
                chapterCode,
                Language.determineLangDirectory(),
                storyType
            )
            phrases = TableOfPhrases(
                activity,
                storyMetadata.storyId,
                chapterCode,
                Language.determineLangDirectory(),
                storyType
            )
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException()
        }
    }

    constructor(
        activity: Activity,
        requestManager: RequestManager,
        chapters: ArrayList<StoryChapter>,
        sutokoParams: SutokoParams,
        card: Game,
        storyMetadata: StoryMetadata,
        tableOfCharacters: TableOfCharacters,
        storyType: StoryType,
        phrases: ArrayList<Phrase>,
        links: ArrayList<TableOfLinks.Link>,
        characters: ArrayList<StoryCharacter>,
        messagesSide: ArrayList<Int>,
        symbols: TableOfSymbols
    )
            : this(
        activity,
        requestManager,
        card,
        storyMetadata,
        sutokoParams,
        tableOfCharacters,
        chapters,
        storyType,
        symbols
    ) {
        this.phrases.array = phrases
        this.links.links = links
        this.characters.characters = characters
        this.adapter.sideHandler.left = messagesSide
        this.adapter.sutokoParams = sutokoParams
    }


    private fun setGameConversationSeenCharacter(character: StoryCharacter?) {
        this.adapter.seenCharacter = character
    }

    private fun searchSeenCharacter(fromPhrase: Phrase): StoryCharacter? {
        var current = Phrase(fromPhrase.getType())
        current.id = fromPhrase.id

        while (true) {
            val l = this.links.getDest(current.id)
            if (l.size == 0) {
                return null
            }
            current = this.phrases.getPhrase(l[0])
            if (current.`is`(Phrase.Type.memory)) {
                val v = current.getVarFromCondition(chapterCode)
                if (v.name == "typingAnimation" && v.value != "true") {
                    return null
                }
            }
            if (!arrayOf(-1, 0).contains(current.id_author)) {
                val character = this.characters.getCharacter(current.id_author)
                if (!character.isMainCharacter && current.id_author != -1) {
                    return character
                }
            }
        }
    }

    private fun updateSeenCharacterIfNecessary(fromPhrase: Phrase?) {
        if (fromPhrase == null || !fromPhrase.`is`(Phrase.Type.memory)) {
            return
        }
        val v = fromPhrase.getVarFromCondition(chapterCode)
        if (v.name == "typingAnimation" && v.value == "true") {
            val character = this.searchSeenCharacter(fromPhrase)
            this.setGameConversationSeenCharacter(character)
        }
    }

    fun startFrom(id: Int, symbols: TableOfSymbols) {
        currentPhrase = phrases.getPhrase(id)
        provokeNext(symbols, true)
    }

    fun start(storyType: StoryType, onError: () -> Unit = {}) {
        if ((storyType == StoryType.CURRENT_USER_STORY || storyType == StoryType.OTHER_USER_STORY) && !phrases.hasId(
                0
            )
        ) {
            Handler(Looper.getMainLooper()).post {
                onError()
            }
            return
        }
        currentPhrase =
            if (storyType == StoryType.CURRENT_USER_STORY || storyType == StoryType.OTHER_USER_STORY) {
                val p = phrases.getPhrase(0)
                if (p.sentence.isEmpty()) {
                    val l = links.getDest(0)
                    if (l.size > 0) {
                        phrases.getPhrase(l[0]);
                    } else {
                        p
                    }
                } else {
                    p
                }
            } else {
                if (SutokoSharedElementsData.STARTING_PHRASE_ID > 0) {
                    phrases.getPhrase(SutokoSharedElementsData.STARTING_PHRASE_ID)
                } else {
                    val l = links.getDest(SutokoSharedElementsData.STARTING_PHRASE_ID)
                    if (l.size == 0) {
                        throw IndexOutOfBoundsException("Index out of bounds for story ${this.card.id} and lang ${Language.determineLangDirectory()}")
                    }
                    phrases.getPhrase(l[0])
                }
            }
    }


    /**
     * Inserts a phrase and fires onPhraseInserted call back
     * @param phrase: Phrase
     */
    fun insert(phrase: Phrase) {
        adapter.insert(phrase)
        callback.onPhraseInserted()
    }

    /**
     * Handles the conversation and fires callback to this end
     * @param symbols : TableOfSymbols
     * @param shouldMove : Boolean (default value : true)
     */
    fun provokeNext(symbols: TableOfSymbols, shouldMove: Boolean = true) {
        isChoiceMode = false


        if (shouldMove) {
            val list = links.getDest(currentPhrase!!.id)
            if (list.size == 0) {
                callback.conversationFinished()
                return
            } else if (phrases.getPhrase(list[0]).id_author == 0) {
                return
            }
            currentPhrase = phrases.getPhrase(list[0])
        }

        Std.debug("Conversation", currentPhrase.toString())

        this.updateSeenCharacterIfNecessary(currentPhrase)

        if (execute("No more phrase found.", currentPhrase == null)) {
            callback.conversationFinished()
            return
        }


        if (execute("Is choice action", currentPhrase!!.isChoicesAction)) {
            val choices = getChoicesAction()
            callback.onChoiceActionsFound(choices)
            return
        }

        if (execute("Is story event", currentPhrase!!.isEvent)) {
            currentPhrase!!.setType(Phrase.Type.event)
            adapter.insert(currentPhrase!!)
            callback.onPhraseInserted()

            if (timeMode == Phrase.TimeUpdateMode.AUTOMATIC) {
                provokeNext(symbols, true)
            }
            return
        }


        if (execute(
                "Message to ignore",
                currentPhrase!!.sentence.isEmpty() && currentPhrase!!.code.isNullOrEmpty() && currentPhrase!!.`is`(
                    Phrase.Type.dest
                )
            )
        ) {
            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute("Register win", currentPhrase!!.isWin)) {
            callback.onRegisterScoreFound(true)
            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute("Character sends vocal message", currentPhrase!!.isCharacterSound)) {

            callback.onVocalMessageFound(currentPhrase!!)
            controlUserChoice()
            return
        }

        if (execute("Register lose", currentPhrase!!.isLost)) {
            callback.onRegisterScoreFound(false)
            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute("Request for fading out the filter.", currentPhrase!!.isFilterFadeOut)) {
            callback.onFilterFadeOut(currentPhrase!!.seen, currentPhrase!!.wait)
            controlUserChoice()
            return
        }

        if (execute("On user input request found.", currentPhrase!!.isUserInputRequest)) {
            callback.onUserInputRequestFound(currentPhrase!!.sentence)
            controlUserChoice()
            return
        }

        if (execute("Update message colors", currentPhrase!!.isMessageColorSwitch)) {
            adapter.currentMessageColor.setValues(currentPhrase!!.getMessageColorText)
            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute("Manga page", currentPhrase!!.isMangaPage)) {
            val p = Phrase(Phrase.Type.mangaPagePreview)
            p.sentence = currentPhrase!!.sentence
            adapter.insert(p)
            callback.onPhraseInserted()
            return
        }

        if (execute("Request for fading in the filter.", currentPhrase!!.isFilterFadeIn)) {
            callback.onFilterFadeIn(currentPhrase!!.seen, currentPhrase!!.wait)
            controlUserChoice()
            return
        }

        if (execute("Request for starting a sound", currentPhrase!!.isSound)) {
            callback.onSoundFound(
                currentPhrase!!.soundName,
                currentPhrase!!.soundChannel,
                currentPhrase!!.soundIsLooping,
                currentPhrase!!.seen
            )

            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute(
                "Request for displaying a sentence on the middle of the screen",
                currentPhrase!!.isNarratorSentence
            )
        ) {
            callback.onNarratorSentenceFound(
                currentPhrase!!.getNarratorSentence,
                currentPhrase!!.getNarratorSentenceMode,
                currentPhrase!!.seen,
                currentPhrase!!.wait
            )
            controlUserChoice()
            return
        }

        if (execute("Request for updating the header's info", currentPhrase!!.isHeaderUpdate)) {
            callback.onHeaderUpdateFound(
                currentPhrase!!.getHeaderUpdateImageFileName,
                currentPhrase!!.getHeaderUpdateTitle,
                currentPhrase!!.getHeaderUpdateSubTitle,
                currentPhrase!!.getHeaderUpdateImageType,
                currentPhrase!!.seen
            )
            controlUserChoice()
            return
        }

        if (execute(
                "Request for updating the header's graphics",
                currentPhrase!!.isHeaderGraphicsUpdate
            )
        ) {
            callback.onHeaderGraphicsUpdateFound(
                currentPhrase!!.getHeaderGraphicsUpdateColor,
                currentPhrase!!.getHeaderGraphicsUpdateAlpha.toInt(),
                currentPhrase!!.getHeaderGraphicsUpdateIconVisibility.toBoolean(),
                currentPhrase!!.seen
            )
            controlUserChoice()
            return
        }

        if (execute("Request for updating an element color", currentPhrase!!.isColorsUpdate)) {
            callback.onColorsUpdateFound(
                currentPhrase!!.getColorsUpdateElement,
                currentPhrase!!.getColorsUpdateColorCode,
                currentPhrase!!.seen
            )
            controlUserChoice()
            return
        }

        if (execute("Request for updating the time mode", currentPhrase!!.isTimeModeUpdate)) {
            timeMode = currentPhrase!!.getTimeUpdateMode
            controlUserChoice()
            if (timeMode == Phrase.TimeUpdateMode.AUTOMATIC) {
                provokeNext(symbols, true)
            }
            return
        }

        if (execute(
                "Request for displaying a background image",
                currentPhrase!!.isBackgroundImage
            )
        ) {
            callback.onBackgroundImageFound(
                currentPhrase!!.backgroundImageName,
                currentPhrase!!.seen
            )

            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute(
                "Request for displaying a background color",
                currentPhrase!!.isBackgroundColor
            )
        ) {
            callback.onBackgroundColorFound(
                currentPhrase!!.backgroundColorHexaCode,
                currentPhrase!!.seen
            )
            controlUserChoice()
            return
        }

        if (execute(
                "Request for updating an element visibility",
                currentPhrase!!.isUpdateElementVisibility
            )
        ) {
            callback.onElementVisibilityUpdateFound(
                currentPhrase!!.getUpdateElementVisibilityType,
                currentPhrase!!.getUpdateElementVisibilityValue,
                currentPhrase!!.seen,
                currentPhrase!!.wait
            )
            controlUserChoice()
            return
        }

        if (execute(
                "Request for loading a CreatorResource as a Background video from url",
                currentPhrase!!.isBackgroundVideoResource
            )
        ) {
            val resource: CreatorResource? =
                creatorResources.getResourceBy(currentPhrase!!.getCreatorResourceId)
            if (resource != null) {
                callback.onBackgroundVideoCreatorResourceFound(resource)
                controlUserChoice()
            } else {
                if (!controlUserChoice()) {
                    provokeNext(symbols)
                }
            }
            return
        }

        if (execute(
                "Request for loading a CreatorResource as a Background image from url",
                currentPhrase!!.isBackgroundImageResource
            )
        ) {
            val resource: CreatorResource? =
                creatorResources.getResourceBy(currentPhrase!!.getCreatorResourceId)
            if (resource != null) {
                callback.onBackgroundImageCreatorResourceFound(resource)
                controlUserChoice()
            } else {
                if (!controlUserChoice()) {
                    provokeNext(symbols)
                }
            }
            return
        }

        if (execute(
                "Request for loading a CreatorResource as a sound from url",
                currentPhrase!!.isSoundResource
            )
        ) {
            val resource: CreatorResource? =
                creatorResources.getResourceBy(currentPhrase!!.getCreatorResourceId)
            if (resource != null) {
                callback.onSoundCreatorResourceFound(resource)
            }
            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute("Request to update the filter", currentPhrase!!.isUpdateFilter)) {
            callback.onFilterUpdateFound(
                currentPhrase!!.getFilterUpdateColor,
                currentPhrase!!.getFilterUpdateAlpha,
                currentPhrase!!.seen
            )
            controlUserChoice()
            return
        }

        if (execute("Request for the phone to vibrate", currentPhrase!!.isPhoneVibrate)) {
            callback.onPhoneVibrateFound(currentPhrase!!.wait, currentPhrase!!.seen)
            controlUserChoice()
            return
        }

        if (execute("Request for a character to hesitate", currentPhrase!!.isHesitating)) {
            callback.onHesitatingCharacterFound(
                currentPhrase!!.id_author,
                currentPhrase!!.seen,
                currentPhrase!!.wait
            )
            return
        }
        if (execute("Request for a character to send a picture", currentPhrase!!.isContentImage)) {
            callback.onImageMessageFound(
                currentPhrase!!.contentImageName,
                currentPhrase!!.id_author,
                currentPhrase!!.seen
            )
            controlUserChoice()
            return
        }

        if (execute("Request for displaying a background video.", currentPhrase!!.isVideo)) {
            callback.onBackgroundVideoFound(currentPhrase!!.getVideo, currentPhrase!!.seen)
            if (!controlUserChoice()) {
                provokeNext(symbols)
            }
            return
        }

        if (execute("Request for saving", currentPhrase!!.`is`(Phrase.Type.memory))) {
            val v = currentPhrase!!.getVarFromCondition(chapterCode)
            if (v.name == "intro" && v.value == "start") {
                callback.onIntroFound(currentPhrase!!.id)
                //  provokeNext(symbols)
                return
            }
            callback.onMemoryToSaveFound(
                currentPhrase!!.id,
                v.name,
                v.value,
                v.storyId,
                currentPhrase!!.seen
            )
            provokeNext(symbols)
            return
        }

        if (execute("Request for change chapter", currentPhrase!!.isNextChapter)) {
            val chapterCode = currentPhrase!!.nextChapterCode
                ?: throw IllegalStateException("Story ${symbols.gameId}/${symbols.chapterCode} - Couldn't get chapter code because it null for phrase id ${currentPhrase!!.id} lang ${Language.determineLangDirectory()}")
            symbols.chapterCode = chapterCode
            callback.onNextChapterFound()
            this.isFinished = true
            return
        }

        if (execute("Found a condition", currentPhrase!!.`is`(Phrase.Type.condition))) {
            currentPhrase = determineConditionResult(symbols, currentPhrase!!.answerCondition)
            provokeNext(symbols, false)
            return
        }

        if (execute("Info phrase found", currentPhrase!!.`is`(Phrase.Type.info))) {
            callback.onPhraseInfoFound(currentPhrase!!)
            return
        }

        if (execute("Fake notification found", currentPhrase!!.isFakeNotification())) {
            val parsedNotification: HashMap<String, String> =
                parseNotification(currentPhrase!!.sentence)
            callback.onFakeNotificationFound(
                parsedNotification["title"] ?: "",
                parsedNotification["subtitle"] ?: "",
                (parsedNotification["imageName"] ?: "").replace(" ", ""),
                parsedNotification["actionText"] ?: "",
                currentPhrase!!.wait
            )
            return
        }

        if (execute("Notification found", currentPhrase!!.isNotification())) {
            if (symbols.isRealTime || BuildConfig.DEBUG) {
                val info = currentPhrase!!.getNotificationInfo()
                val character = characters.getCharacter(currentPhrase!!.id_author)
                callback.onNotificationFound(
                    // Story id
                    storyMetadata.storyId,
                    character,
                    // Notification content
                    info[0],
                    // Notification incremental days from today
                    Integer.parseInt(info[1]),
                    // Hour
                    Integer.parseInt(info[2]),
                    // Min
                    Integer.parseInt(info[3]),
                    info[4]
                )
            }
            provokeNext(symbols, true)
            return
        }

        execute("Inserting a simple message" + currentPhrase.toString()) {
            callback.onSimpleMessageFound(currentPhrase!!)
        }
    }

    /**
     * Returns the choices action
     * @return ArrayList<ChoiceAction>
     */
    private fun getChoicesAction(): ArrayList<ChoiceAction> {
        val array: ArrayList<ChoiceAction> = ArrayList()
        this.links.getDest(currentPhrase!!.id).forEach { id ->
            val phrase = phrases.getPhrase(id)
            val choice = ChoiceAction(phrase.code ?: "", phrase.sentence, id)
            array.add(choice)
        }

        return array
    }

    fun getMangaMessages(symbols: TableOfSymbols): ArrayList<MangaMessage> {
        val array = ArrayList<MangaMessage>()

        val messagesIds = links.getDest(this.currentPhrase!!.id)

        messagesIds.forEach { id ->
            val phrase = phrases.getPhrase(id)
            val message =
                com.purpletear.smsgame.activities.manga.MangaHelper.parseMessage(phrase, symbols)
            if (message != null) {
                array.add(message)
            }
        }

        return array
    }

    fun continueAfterMangaPage(symbols: TableOfSymbols) {
        val messagesIds = links.getDest(this.currentPhrase!!.id)

        messagesIds.forEach { id ->
            val phrase = phrases.getPhrase(id)
            if (!phrase.isMangaMessage) {
                currentPhrase = phrase
                provokeNext(symbols, false)
                return
            }
        }
        callback.conversationFinished()
    }

    /**
     * Parse notification text into an hashmap
     *
     * @param text
     * @return
     */
    private fun parseNotification(text: String): HashMap<String, String> {
        val a1 = text.split("\n")
        val map = HashMap<String, String>()
        a1.forEach {
            val tmp = it.split(":")
            if (tmp.size == 2) {
                map[tmp[0]] = tmp[1]
            }
        }
        return map
    }


    /**
     * Determines if the set time mode is automatic
     * @return Boolean
     */
    fun isAutomaticTimeMode(): Boolean {
        return this.timeMode == Phrase.TimeUpdateMode.AUTOMATIC
    }


    /**
     * Handles the event isUserChoice
     */
    fun controlUserChoice(): Boolean {
        if (isUserChoice()) {
            val ids = links.getDest(currentPhrase!!.id)
            isChoiceMode = true
            callback.onChoicesToMakeFound(phrases.getPhrases(ids))
            return true
        }
        return false
    }

    /**
     * Determines if the next Phrase are user choices
     * @return Boolean
     */
    private fun isUserChoice(): Boolean {
        val l = links.getDest(currentPhrase!!.id)
        if (l.size == 1 && phrases.getPhrase(l[0]).`is`(Phrase.Type.choice)) {
            this.currentPhrase = phrases.getPhrase(l[0])
            return true
        }
        return links.getDest(currentPhrase!!.id).size > 1 && !currentPhrase!!.isChoicesAction
    }

    /**
     * Determines the result of the given condition
     * @param symbols : TableOfSymbols
     * @param values : Array<String?>
     * @return Phrase
     */
    fun determineConditionResult(symbols: TableOfSymbols, values: Array<String?>): Phrase? {
        val condition =
            values[0]!!.replace("[", "").replace("]", "").replace(" ", "").split("==".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        val mThen = Integer.parseInt(values[1]!!)
        val mElse = Integer.parseInt(values[2]!!)

        return if (symbols.condition(storyMetadata.storyId, condition[0], condition[1])) {
            phrases.getPhrase(mThen)
        } else {
            phrases.getPhrase(mElse)
        }
    }

    companion object {

        /**
         * Does the same as an if statement but also log a message
         * @param message : String
         * @param when : Boolean
         * @return Bool
         */
        private fun execute(message: String, `when`: Boolean): Boolean {
            if (BuildConfig.DEBUG && `when`) {
                Log.i(this::class.java.simpleName, message)
            }
            return `when`
        }

        /**
         * Executes a function and logs a message
         * @param message : String
         * @param function : () -> Unit
         */
        private fun execute(message: String, function: () -> Unit) {
            if (BuildConfig.DEBUG) {
                Log.i(this::class.java.simpleName, message)
            }
            function()
        }

    }
}