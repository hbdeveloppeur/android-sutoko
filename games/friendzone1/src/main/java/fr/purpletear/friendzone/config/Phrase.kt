/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.config

import android.os.Parcel
import android.os.Parcelable
import fr.purpletear.friendzone.tables.Language
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols
import java.util.regex.Pattern


class Phrase : Parcelable {

    /**
     * Phrase's id
     */
    var id: Int = 0
        private set

    /**
     * The id of the author of the hrase.
     */
    var id_author: Int = 0
        private set

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
    var code: String = ""

    /**
     * Is the phrase a condition ?
     *
     * @return true if it is a condition
     */
    private val isCondition: Boolean
        get() =
            looksLikeACode() && sentence.matches(("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]").toRegex())

    val isTrophy : Boolean
    get() =
        looksLikeACode() && sentence.startsWith("[TROPHY\$\$\$")  && (sentence.split("$$$")[1].replace("]", "") != "NaN")
    val trophyId : Int
    get() {
        return sentence.split("$$$")[1].replace("]", "").toInt()
    }


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

            val p2 = Pattern.compile("\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}")
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

    /**
     * Gets type
     * @return Type
     */
    fun getType(): Type {
        return determineTypeEnum(type)
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
        get() = looksLikeACode() && sentence.matches(("\\[([a-z0-9A-Z_]+).png\\]").toRegex())

    /**
     * Determines if the user lost the game
     * @return Boolean
     */
    fun isLost(): Boolean {
        val tmp = sentence.replace(" ", "")
        return tmp == "[LOST]" || tmp == "[LOSE]"
    }

    /**
     * Determines if the user sends the screenshot to Eva's father
     */
    fun isScreenShot(): Boolean {
        return sentence.replace(" ", "").equals("[SENDSCREENSHOT]")
    }

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
            return sentence.replace("[", "").replace(".png]", "")
        }

    /**
     * Determines if the phrase is of type Offline
     *
     * @return true if it is.
     */
    fun isOffline(): Boolean {
        val tmp = sentence.replace(" ", "")
        return tmp == "[OFFLINE]"
    }

    /**
     * Determines if the phrase is "banned"
     * @return Boolean
     */
    fun isBanned(): Boolean {
        return sentence.replace(" ", "") == "[BANNED]"
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

    /**
     * Determines if the phrase is a next Chapter.
     *
     * @return true if it is
     */
    val isNextChapter: Boolean
        get() = (sentence == "[ACTION-3]")


    val nextChapter: String
        get() = code.replace(" ", "")

    internal val isSound: Boolean
        get() = looksLikeACode() && sentence.endsWith(".mp3]")

    internal val soundName: String
        get() {
            if (!isSound) {
                throw IllegalStateException("Calling getSound on a non-sound")
            }
            return sentence.replace("[", "").replace(".mp3]", "")
        }

    internal val jumpToId: Int
        get() = Integer.parseInt(sentence
                .replace(" ", "")
                .replace("[JUMPTOID_", "")
                .replace("]", ""))

    val isBackgroundImage: Boolean
        get() {
            val s = sentence.replace(" ", "")
            return (s.endsWith(".jpeg]") || s.endsWith(".jpg]"))
        }

    val backgroundImageName: String
        get() {
            if (!isBackgroundImage) {
                throw IllegalStateException()
            }
            return sentence.replace(" ", "").replace("[BACKGROUND_", "").replace(".jpeg]", "").replace(".jpg]", "")
        }

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
        action,
        typing,
        meImage,
        nextChapter,
        me,
        meSeen,
        undetermined,
        trophy
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
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(id_author)
        dest.writeString(sentence)
        dest.writeInt(seen)
        dest.writeInt(wait)
        dest.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    private constructor(id: Int, id_author: Int, sentence: String, seen: Int, wait: Int, type: Int) {
        this.id = id
        this.id_author = id_author
        this.sentence = sentence
        this.seen = seen
        this.wait = wait
        this.type = type
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
        if (sentence.replace(" ", "") == "[SCREENSHOT]") return true
        if (sentence.replace(" ", "") == "") return true
        if (sentence.length == 0) return true
        if (sentence[0] == '(' && sentence[sentence.length - 1] == ')' && id_author == 0)
            return true
        return if (id_author == 0) false else false
    }

    internal fun formatVars(table: TableOfSymbols) {
        for (`var` in table.getArray(GlobalData.Game.FRIENDZONE.id)) {
            sentence = sentence.replace("[[" + `var`.n + "]]", `var`.v)
        }
    }

    /**
     * Tells if the sentence looks like a piece of code
     * use this to gain time and avoid using matches
     */
    private fun looksLikeACode(): Boolean {
        return sentence.isNotEmpty() && sentence[0] == '['

    }

    /**
     * @param chapterNumber the number of the current chapter
     * @return the var concerned
     */
    fun getVarFromCondition(chapterNumber: Int): Var {
        if (!isCondition) {
            throw IllegalArgumentException("CallinggetVarFromConditon on a non isCondition phrase " + sentence)
        }

        val pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]")

        val m = pattern.matcher(sentence)
        if (!m.find()) {
            throw IllegalArgumentException("Phrase.getVarFromCondition.find")
        }

        val toSplit = m.group().replace("[", "").replace("]", "").replace(" ", "")
        val v = toSplit.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Var(v[0], v[1], chapterNumber)
    }

    /**
     * Determines if the sentence is an announcement
     * @return Boolean
     */
    fun isAnnouncement(): Boolean {
        return sentence.replace(" ", "") == "[ANNOUNCEMENT]"
    }

    /**
     * Returns the sentence without (info)
     *
     * @return String
     */
    internal fun withoutInfo(): String {
        val pattern = Pattern.compile("(\\(.*\\)[ ]*)")
        val m = pattern.matcher(sentence)
        return if (!m.find()) {
            sentence
        } else sentence.replace(m.group(), "")
    }

    /**
     * Replace "[prenom]" by the name specified in the paramters
     *
     * @param replacement replace the target with this
     */
    internal fun formatName(symbols : TableOfSymbols) {
        sentence = sentence.replace("[prenom]", symbols.globalFirstName)
    }

    /**
     * Determines if the current Phrase is of type <type>
     *
     * @return boolean
    </type> */
    fun `is`(type: Type): Boolean {
        return type == Phrase.determineTypeEnum(this.type)
    }

    fun setType(type: Type) {
        this.type = Phrase.determineTypeCode(type)
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
    fun isMp4(): Boolean {
        return sentence.replace(" ", "").endsWith(".mp4]")
    }

    /**
    Returns the sound name
    @return String
     */
    fun getMp4(): String {
        return sentence
                .replace(" ", "")
                .replace(".mp4]", "")
                .replace("[", "")
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


        /* **************/

        /**
         * Builds a phrase easily
         *
         * @param id_author int
         * @param type      int
         * @param sentence  String
         * @return Phrase
         */
        fun fast(id_author: Int, type: Phrase.Type, sentence: String, seen: Int, wait: Int): Phrase {
            return Phrase(-1, id_author, sentence, seen, wait, Phrase.determineTypeCode(type))
        }

        /**
         * Determines the type of the phrase
         *
         * @return Type
         */
        fun determineTypeEnum(typeCode: Int): Type = when (typeCode) {
            0 -> Type.typing
            1 -> Type.dest
            2 -> Type.condition
            3 -> Type.memory
            4 -> Type.info
            5 -> Type.image
            6 -> Type.action
            8 -> Type.me
            9 -> Type.nextChapter
            10 -> Type.meSeen
            11 -> Type.meImage
            12 -> Type.trophy
            else -> Type.undetermined
        }

        /**
         * Determines the code given the type
         *
         * @param type Type
         * @return int
         */
        fun determineTypeCode(type: Type): Int {
            return when (type) {
                Phrase.Type.typing -> 0
                Phrase.Type.dest -> 1
                Phrase.Type.condition -> 2
                Phrase.Type.memory -> 3
                Phrase.Type.info -> 4
                Phrase.Type.image -> 5
                Phrase.Type.action -> 6
                Phrase.Type.me -> 8
                Phrase.Type.nextChapter -> 9
                Phrase.Type.meSeen -> 10
                Phrase.Type.meImage -> 11
                Type.trophy -> 12
                else -> -1
            }
        }

        /**
         * NextChapterDelay
         */
        fun nextChapterDelay(): Int {
            if(Language.determineLangCode()== Language.Code.JA) {
                return 9000
            }
            return 7000
        }

    }

    /**
     * Determines if the Phrase is a call to start the Bryan Game
     * @return Boolean
     */
    fun isSaveZoeGame(): Boolean {
        return sentence.replace(" ", "") == "[SAVEZOE.GAME]"
    }

    /**
     * Returns the seen value controller by lang
     */
    fun getSeenWithLangControl(): Int {
        if(Language.determineLangCode() == Language.Code.JA) {
            return (seen * 1.7).toInt()
        }
        return seen
    }

    /**
     * Returns the seen value controller by lang
     */
    fun getWaitWithLangControl(): Int {
        if(Language.determineLangCode() == Language.Code.JA) {
            return (wait * 1.7).toInt()
        }
        return wait
    }

    /**
     * Determines if the phrase is a call to the glitch animation
    @return Boolean
     */
    fun isGlitchAnimation(): Boolean {
        return sentence.replace(" ", "") == "[ANIMATION1]"
    }


    /**
     * Determines if the Phrase is a call to set Zoé's Image
     * @param n : Int
     */
    fun isZoeSetImage(n: Int): Boolean {
        return sentence.replace(" ", "") == "[SETIMAGEZOE$n]"
    }
}
