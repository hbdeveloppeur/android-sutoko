/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.purpletear.smsgame.activities.smsgame.objects

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCreatorResources
import java.util.*
import java.util.regex.Pattern

@Keep
class Phrase : Parcelable {

    /**
     * Phrase's id
     */
    var id: Int = 0

    /**
     * The id of the author of the Phrase.
     */
    var id_author: Int = 0

    /**
     * The content of the sentence.
     */
    var sentence: String = ""

    /**
     * How long to see the message
     */
    var seen: Int = 0

    /**
     * The time the person waits before answering.
     */
    var wait: Int = 0

    /**
     * The type of the phrase.
     */
    var type: Int = 0

    /**
     * The type of the phrase.
     */
    var time: String? = ""

    var code: String? = ""


    /**
     * Is the phrase a condition ?
     *
     * @return true if it is a condition
     */
    private val isCondition: Boolean
        get() =
            looksLikeACode() && sentence.matches(("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]").toRegex())

    val isSecretChoice: Boolean
        get() =
            code != null && code!!.replace(" ", "").startsWith("secret:")
    val secretChoiceDiamonds: Int
        get() =
            code!!.replace(" ", "").replace("secret:", "").toInt()

    /**
     * Is the Phrase an asnwer with a condition inside ?
     *
     * @return true if it is
     */
    private val isAnswerCondition: Boolean
        get() =
            looksLikeACode() && sentence.matches(("\\[([a-zA-Z0-9]+)([ ]*)==([ ]*)([a-zéA-Z0-9]+)\\]([ ]*)\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}([ ]*)\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}").toRegex())

    val answerCondition: Array<String?>
        get() {
            if (!isAnswerCondition) {
                throw IllegalArgumentException("Calling getAnswerCondition on a non AnswerCondition")
            }


            val pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)==([ ]*)([a-zéA-Z0-9]+)\\]")
            val m = pattern.matcher(sentence)

            if (!m.find()) throw IllegalArgumentException("Phrase.getAnswerCondtion.find error")

            val condition = m.group()

            val p2 =
                Pattern.compile("\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}")
            val m2 = p2.matcher(sentence)
            val values = arrayOfNulls<String>(3)
            values[0] = condition

            var i = 1
            while (m2.find()) {
                values[i] = m2.group().replace("{", "").replace("}", "")
                i++
            }
            return values
        }

    val resourceId: Int
        get() = sentence.replace("[", "").replace("]", "").toInt()

    val isBackgroundVideoResource: Boolean
        get() = code != null && PhraseFromCreatorResource.getCode(TableOfCreatorResources.CreatorResourceType.VIDEOS.rname) == code!!.lowercase(
            Locale.ENGLISH
        )
    val isBackgroundImageResource: Boolean
        get() = code != null && PhraseFromCreatorResource.getCode(TableOfCreatorResources.CreatorResourceType.IMAGES.rname) == code!!.lowercase(
            Locale.ENGLISH
        )
    val isSoundResource: Boolean
        get() = code != null && PhraseFromCreatorResource.getCode(TableOfCreatorResources.CreatorResourceType.SOUNDS.rname) == code!!.lowercase(
            Locale.ENGLISH
        )
    val isMessageColorSwitch: Boolean
        get() = code != null && code == "message_background_update"
    val isMangaPage: Boolean
        get() = code != null && code == "manga-page"
    val isMangaMessage: Boolean
        get() = code != null && code == "manga-message"
    val isEvent: Boolean
        get() = code != null && code == "event"
    val isWin: Boolean
        get() = sentence == "[ACTION-4]"
    val isLost: Boolean
        get() = sentence == "[ACTION-5]"
    val getMessageColorText: String
        get() = sentence
    val getCreatorResourceId: Int
        get() = sentence.replace("[", "").replace("]", "").toInt()
    val isCharacterSound: Boolean
        get() = code != null && code!!.replace(" ", "") == "sound"


    val isColorsUpdate: Boolean
        get() = sentence.startsWith("[COLORS$$$")

    enum class ColorsUpdateElement {
        STORY_TEXTS,
        BUTTON_TEXT
    }

    val getColorsUpdateElement: ColorsUpdateElement
        get() {
            return when (sentence.split("$$$")[1].replace("]", "").toInt()) {
                1 -> ColorsUpdateElement.STORY_TEXTS
                2 -> ColorsUpdateElement.BUTTON_TEXT
                else -> throw IllegalArgumentException("ColorsUpdateElement not handled : $sentence")
            }
        }

    val getColorsUpdateColorCode: String
        get() {
            return sentence.split("$$$")[2].replace("]", "")
        }

    val isTimeModeUpdate: Boolean
        get() = sentence.startsWith("[TIME$$$")

    enum class TimeUpdateMode {
        AUTOMATIC,
        MANUAL
    }

    val getTimeUpdateMode: TimeUpdateMode
        get() {
            return when (sentence.split("$$$")[1].replace("]", "").lowercase()) {
                "auto" -> TimeUpdateMode.AUTOMATIC
                "manual" -> TimeUpdateMode.MANUAL
                else -> throw IllegalArgumentException("Time Update Mode not handled : $sentence")
            }
        }

    /**
     * Gets type
     * @return Type
     */
    fun getType(): Type {
        return determineTypeEnum(type, this.id_author)
    }

    /**
     * Gets the type
     * @return Int
     */
    fun getIntType(): Int {
        return type
    }

    /**
     * Determines if it is a Content image.
     * [imagename1234.png]
     *
     * @return true if it is a content image
     */
    val isContentImage: Boolean
        get() = sentence.startsWith("[MEDIA-3-")

    /**
     * Determines if it is an action
     *
     * @return true if it is a content image
     */
    val isHesitating: Boolean
        get() = sentence.startsWith("[ACTION-1]")

    val isChoicesAction: Boolean
        get() = code != null && code == "choice_action"

    /**
     * Determines if it is a request for fade out
     *
     * @return true if it is a content image
     */
    val isFilterFadeOut: Boolean
        get() = sentence == "[FADE-1]"

    val isUserInputRequest: Boolean
        get() = code != null && code == "input"

    /**
     * Determines if it is a Content image.
     * [imagename1234.png]
     *
     * @return true if it is a content image
     */
    val isFilterFadeIn: Boolean
        get() = sentence == "[FADE-0]"


    /**
     * Returns the name of the image for a ContentImage
     *
     * @return String
     */
    val contentImageName: String
        get() {
            if (!isContentImage) {
                throw IllegalStateException("Calling getContentImageName > " + toString())
            }
            return sentence.replace("[MEDIA-3-", "").replace("]", "")
        }

    /**
     * Determines if there is a condition on the choice 's sentence
     *
     * @return true if it is.
     */
    internal val isChoiceEqualCondition: Boolean
        get() =
            looksLikeACode() && sentence.matches(("^\\[([0-9a-zA-Z_]*)([ ]*)==([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*").toRegex())

    /**
     * Determines if there is a not equal condition on the choice 's sentence
     *
     * @return true if it is.
     */
    internal val isChoiceNotEqualCondition: Boolean
        get() =
            looksLikeACode() && sentence.matches(("^\\[([0-9a-zA-Z_]*)([ ]*)!=([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*").toRegex())


    /**
     * @param storyId
     * @return the var concerned
     */
    fun getVarFromCondition(chapterCode: String): com.purpletear.smsgame.activities.smsgame.objects.Var {
        if (!isCondition) {
            throw IllegalArgumentException("Calling getVarFromConditon on a non isCondition phrase " + sentence)
        }

        val pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]")

        val m = pattern.matcher(sentence)
        if (!m.find()) {
            throw IllegalArgumentException("Phrase.getVarFromCondition.find")
        }

        val toSplit = m.group().replace("[", "").replace("]", "").replace(" ", "")
        val v = toSplit.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return com.purpletear.smsgame.activities.smsgame.objects.Var(v[0], v[1], chapterCode, seen)
    }

    /**
     * Return the condition in a String object
     * For exemple :
     * "[name == value]MySentence" will return [name == value]
     */
    val choiceEqualCondition: String
        get() {
            if (!isChoiceEqualCondition) {
                throw IllegalArgumentException("Calling getChoiceCondition on a non choiceCondition sentence")
            }

            val p = Pattern.compile("^\\[([0-9a-zA-Z_]+)([ ]*)==([ ]*)([0-9a-zA-Z_]+)\\]([ ]*)")
            val m = p.matcher(sentence)
            if (!m.find()) {
                throw IllegalArgumentException("Phrase.getChoiceCondition.find didin't work with " + p.toString())
            }

            return m.group().replace(" ", "")
        }

    /**
     * Return the not equal condition in a String object
     * For exemple :
     * "[name == value]MySentence" will return [name == value]
     */
    val choiceNotEqualCondition: String
        get() {
            if (!isChoiceNotEqualCondition) {
                throw IllegalArgumentException("Calling getChoiceCondition on a non choiceCondition sentence")
            }

            val p = Pattern.compile("^\\[([0-9a-zA-Z_]+)([ ]*)!=([ ]*)([0-9a-zA-Z_]+)\\]([ ]*)")
            val m = p.matcher(sentence)
            if (!m.find()) {
                throw IllegalArgumentException("Phrase.getChoiceCondition.find didn't work with " + p.toString())
            }

            return m.group().replace(" ", "")
        }

    /**
     * @return the sentence without the condition
     */
    internal val choiceEqualConditionFormated: String
        get() = sentence.replace(" == ", "==").replace(choiceEqualCondition, "")

    internal val isBackgroundImage: Boolean
        get() = sentence.startsWith("[MEDIA-2-")

    internal val isBackgroundColor: Boolean
        get() = sentence.startsWith("[MEDIA-5-")

    internal val isPhoneVibrate: Boolean
        get() = sentence == ("[ACTION-2]")

    internal val isSound: Boolean
        get() = sentence.startsWith("[AUDIO$$$")


    internal val soundName: String
        get() {
            if (!isSound) {
                throw IllegalStateException("Calling getSound on a non-sound")
            }
            return sentence
                .split("$$$")[3].replace("]", "")
        }

    internal val soundChannel: Int
        get() {
            if (!isSound) {
                throw IllegalStateException("Calling getSound on a non-sound")
            }
            return sentence
                .split("$$$")[1].replace("]", "").toInt()
        }

    internal val soundIsLooping: Boolean
        get() {
            if (!isSound) {
                throw IllegalStateException("Calling getSound on a non-sound")
            }
            return sentence
                .split("$$$")[2].replace("]", "").lowercase() == "true"
        }


    internal val isHeaderGraphicsUpdate: Boolean
        get() = sentence.startsWith("[HEADER-GRAPHICS$$$")

    internal val getHeaderGraphicsUpdateColor: String
        get() {
            if (!isHeaderGraphicsUpdate) {
                throw java.lang.IllegalStateException("Calling getNarratorSentence on a non-headerGraphicsUpdate")
            }
            return sentence.split("$$$")[1].replace("]", "")
        }

    internal val getHeaderGraphicsUpdateAlpha: String
        get() {
            if (!isHeaderGraphicsUpdate) {
                throw java.lang.IllegalStateException("Calling getNarratorSentence on a non-headerGraphicsUpdate")
            }
            return sentence.split("$$$")[2].replace("]", "")
        }

    internal val getHeaderGraphicsUpdateIconVisibility: String
        get() {
            if (!isHeaderGraphicsUpdate) {
                throw java.lang.IllegalStateException("Calling getNarratorSentence on a non-headerGraphicsUpdate")
            }
            return sentence.split("$$$")[3].replace("]", "")
        }

    internal val isNarratorSentence: Boolean
        get() = sentence.startsWith("[SENTENCE$$$")

    internal val getNarratorSentence: String
        get() {
            if (!isNarratorSentence) {
                throw java.lang.IllegalStateException("Calling getNarratorSentence on a non-narratorSentence")
            }
            return sentence.split("$$$")[2].replace("]", "")
        }

    internal val isHeaderUpdate: Boolean
        get() = sentence.startsWith("[HEADER$$$")

    internal val getHeaderUpdateTitle: String
        get() {
            if (!isHeaderUpdate) {
                throw java.lang.IllegalStateException("Calling getHeaderUpdateTitle on a non-isHeaderUpdate")
            }
            return sentence.split("$$$")[1].replace("]", "")
        }

    internal val getHeaderUpdateSubTitle: String
        get() {
            if (!isHeaderUpdate) {
                throw java.lang.IllegalStateException("Calling getHeaderUpdateTitle on a non-isHeaderUpdate")
            }
            return sentence.split("$$$")[2].replace("]", "")
        }

    internal val getHeaderUpdateImageFileName: String
        get() {
            if (!isHeaderUpdate) {
                throw java.lang.IllegalStateException("Calling getHeaderUpdateTitle on a non-isHeaderUpdate")
            }
            return sentence.split("$$$")[3].replace("]", "")
        }

    internal val getHeaderUpdateImageType: PhraseUpdateHeaderImageType
        get() {
            if (!isHeaderUpdate) {
                throw java.lang.IllegalStateException("Calling getHeaderUpdateTitle on a non-isHeaderUpdate")
            }
            return when (sentence.split("$$$")[4].replace("]", "").toInt()) {
                1 -> PhraseUpdateHeaderImageType.CHARACTER_AVATAR
                2 -> PhraseUpdateHeaderImageType.MEDIA_FILE
                else -> PhraseUpdateHeaderImageType.NONE
            }
        }

    enum class NarratorSentenceMode {
        CENTER,
        BOTTOM
    }

    enum class PhraseUpdateHeaderImageType {
        CHARACTER_AVATAR,
        MEDIA_FILE,
        NONE
    }

    internal val getNarratorSentenceMode: NarratorSentenceMode
        get() {
            if (!isNarratorSentence) {
                throw java.lang.IllegalStateException("Calling getNarratorSentence on a non-narratorSentence")
            }
            return when (sentence.split("$$$")[1]) {
                "center" -> NarratorSentenceMode.CENTER
                "bottom" -> NarratorSentenceMode.BOTTOM
                else -> throw IllegalArgumentException("${sentence.split("$$$")[1]} Illegal")
            }
        }

    val backgroundImageName: String
        get() {
            if (!isBackgroundImage) {
                throw IllegalStateException()
            }
            return sentence.replace(" ", "").replace("[MEDIA-2-", "")
                .replace("]", "")
        }

    val backgroundColorHexaCode: String
        get() {
            if (!isBackgroundColor) {
                throw IllegalStateException()
            }
            return sentence.replace(" ", "").replace("[MEDIA-5-", "")
                .replace("]", "")
        }

    val isUpdateElementVisibility: Boolean
        get() = sentence.startsWith("[GHOST-")

    enum class VisibilityValue {
        VISIBLE,
        INVISIBLE,
        GONE
    }

    val getUpdateElementVisibilityValue: VisibilityValue
        get() {
            return when (val v = (sentence.split("-")[2].replace("]", "")).lowercase()) {
                "visible" -> VisibilityValue.VISIBLE
                "invisible" -> VisibilityValue.INVISIBLE
                "gone" -> VisibilityValue.GONE
                else -> throw IllegalArgumentException("Not handled visibility $v")
            }
        }

    enum class UpdateElementVisibilityType {
        HEADER,
        RECYCLERVIEW,
        BUTTON,
        BUTTON_ICON,
        NONE
    }

    val getUpdateElementVisibilityType: UpdateElementVisibilityType
        get() {
            return when (sentence.split("-")[1].replace("]", "").toInt()) {
                1 -> UpdateElementVisibilityType.HEADER
                2 -> UpdateElementVisibilityType.RECYCLERVIEW
                3 -> UpdateElementVisibilityType.BUTTON
                4 -> UpdateElementVisibilityType.BUTTON_ICON
                else -> UpdateElementVisibilityType.NONE
            }
        }

    val isUpdateFilter: Boolean
        get() = sentence.startsWith("[FILTER$$$")

    val getFilterUpdateColor: String
        get() = sentence.split("$$$")[1].replace("]", "")

    val getFilterUpdateAlpha: Int
        get() = sentence.split("$$$")[2].replace("]", "").toInt()

    /**
     * Determines if the phrase is an action to ban the player
     *
     * @return boolean
     */
    internal val isBan: Boolean
        get() = sentence == "[BANNED]"

    /**
     * The type of the phrase.
     * /!\ You have to keep a certain logique between the Java code and javascript website code
     * see determineType
     */
    enum class Type {
        dest,
        condition,
        memory,
        info,
        image,
        imageMe,
        action,
        me,
        isTyping,
        meExtended,
        makeChoice,
        end,
        effect,
        rate,
        actionChoice,
        mangaPagePreview,
        event,
        nextChapter,
        vocal,
        vocalMe,
        choice
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }

        if (other !is Phrase) {
            return false
        }

        val o = other as Phrase?

        return (o!!.id == id
                && o.id_author == id_author
                && o.sentence == sentence)
    }

    /* ************/
    protected constructor(`in`: Parcel) {
        id = `in`.readInt()
        id_author = `in`.readInt()
        sentence = `in`.readString() ?: ""
        seen = `in`.readInt()
        wait = `in`.readInt()
        type = `in`.readInt()
        time = `in`.readString() ?: ""
        code = `in`.readString() ?: ""
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(id_author)
        dest.writeString(sentence)
        dest.writeInt(seen)
        dest.writeInt(wait)
        dest.writeInt(type)
        dest.writeString(time)
        dest.writeString(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor(id: Int, id_author: Int, sentence: String, seen: Int, wait: Int, type: Int) {
        this.id = id
        this.id_author = id_author
        this.sentence = sentence
        this.seen = seen
        this.wait = wait
        this.type = type
    }

    constructor(id: Int, characterId: Int, type: Type, sentence: String) {
        this.id = id
        this.id_author = characterId
        this.sentence = sentence
        this.seen = 0
        this.wait = 0
        this.type = determineTypeCode(type)
    }

    constructor(type: Type) {
        this.id = 0
        this.id_author = -1
        this.sentence = ""
        this.seen = 0
        this.wait = 0
        this.type = determineTypeCode(type)
    }

    /**
     * Determines if the sentence can be display or not.
     *
     * @return true if the sentence needs to be skipped
     */
    fun needsSkip(): Boolean {
        if (getType() == Type.choice) return false
        if (getType() == Type.end) return false
        if (getType() == Type.nextChapter) return false
        if (getType() == Type.actionChoice) return false
        if (getType() == Type.mangaPagePreview) return false
        if (isChoicesAction) return false
        if (getType() == Type.rate) return false
        if (getType() == Type.makeChoice) return false
        if (getType() == Type.isTyping) return false
        if (sentence.replace(" ", "") == "") return true
        if (sentence[0] == '(' && sentence[sentence.length - 1] == ')')
            return true
        return false
    }

    /**
     * Tells if the sentence looks like a piece of code
     * use this to gain time and avoid using matches
     */
    private fun looksLikeACode(): Boolean {
        return sentence.isNotEmpty() && sentence[0] == '['
    }

    /**
     * Replace "[prenom]" by the name specified in the paramters
     *
     * @param replacement replace the target with this
     */
    internal fun formatName(replacement: String) {
        sentence = sentence.replace("[prenom]", replacement)
    }

    /**
     * Determines if the current Phrase is of type <type>
     *
     * @return boolean
    </type> */
    fun `is`(type: Type): Boolean {
        return type == Phrase.determineTypeEnum(this.type, this.id_author)
    }

    fun isNotification(): Boolean {
        return code != null && code == "notification"
    }

    fun isFakeNotification(): Boolean {
        return code == "fake_notification"
    }

    fun getNotificationInfo(): List<String> {
        return sentence.split("\n")
    }

    fun setType(type: Type) {
        this.type = determineTypeCode(type)
    }

    override fun toString(): String {
        return "Phrase{" +
                "id=" + id +
                ", id_author=" + id_author +
                ", sentence='" + sentence + '\''.toString() +
                ", seen=" + seen +
                ", wait=" + wait +
                ", type=" + type +
                '}'.toString()
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + id_author
        result = 31 * result + sentence.hashCode()
        result = 31 * result + seen
        result = 31 * result + wait
        result = 31 * result + type
        return result
    }


    /**
    Determines if the phrase is a call to an mp4 file
    @return Bool
     */
    internal val isVideo: Boolean
        get() {
            return sentence.startsWith("[MEDIA-1-")
        }

    /**
    Determines if the phrase is a call to an mp4 file
    @return Bool
     */
    internal val isNextChapter: Boolean
        get() {
            return sentence == "[ACTION-3]"
        }

    /**
    Determines if the phrase is a call to an mp4 file
    @return Bool
     */
    internal val nextChapterCode: String?
        get() {
            return code
        }

    /**
    Returns the sound name
    @return String
     */
    internal val getVideo: String
        get() {
            return sentence
                .replace(" ", "")
                .replace("]", "")
                .replace("[MEDIA-1-", "")
        }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Phrase> = object : Parcelable.Creator<Phrase> {
            override fun createFromParcel(`in`: Parcel): Phrase {
                return Phrase(`in`)
            }

            override fun newArray(size: Int): Array<Phrase?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * Determines the type of the phrase
         *
         * @return Type
         */
        fun determineTypeEnum(typeCode: Int, id_author: Int): Type = when (typeCode) {
            1 -> {
                if (id_author == 0) Type.me else Type.dest
            }

            2 -> Type.condition
            3 -> Type.memory
            4 -> Type.info
            5 -> Type.image
            6 -> Type.action
            9 -> Type.imageMe
            12 -> Type.isTyping
            13 -> Type.meExtended
            15 -> Type.makeChoice
            16 -> Type.end
            17 -> Type.effect
            18 -> Type.rate
            19 -> Type.actionChoice
            20 -> Type.mangaPagePreview
            21 -> Type.event
            22 -> Type.nextChapter
            23 -> Type.vocal
            24 -> Type.choice
            25 -> Type.vocalMe
            else -> Type.me
        }

        /**
         * Determines the code given the type
         *
         * @param type Type
         * @return int
         */
        fun determineTypeCode(type: Type): Int {
            return when (type) {
                Type.dest -> 1
                Type.condition -> 2
                Type.memory -> 3
                Type.info -> 4
                Type.image -> 5
                Type.action -> 6
                Type.me -> 1
                Type.isTyping -> 12
                Type.meExtended -> 14
                Type.makeChoice -> 15
                Type.end -> 16
                Type.effect -> 17
                Type.rate -> 18
                Type.imageMe -> 9
                Type.actionChoice -> 19
                Type.mangaPagePreview -> 20
                Type.event -> 21
                Type.nextChapter -> 22
                Type.vocal -> 23
                Type.choice -> 24
                Type.vocalMe -> 25
            }
        }
    }
}
