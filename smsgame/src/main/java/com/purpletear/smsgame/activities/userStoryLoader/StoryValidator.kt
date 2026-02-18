package com.purpletear.smsgame.activities.userStoryLoader

import android.app.Activity
import androidx.annotation.Keep
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.adapter.AdapterSideHandler
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCharacters
import com.purpletear.smsgame.activities.smsgame.tables.TableOfLinks
import com.purpletear.smsgame.activities.smsgame.tables.TableOfPhrases
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import purpletear.fr.purpleteartools.Language
import java.lang.reflect.Type
import java.util.*

@Keep
@Suppress("JoinDeclarationAndAssignment")
class StoryValidator(
    activity: Activity,
    chapterCode: String,
    langCode: String,
    storyType: StoryType,
    val storyId: Int
) {
    private val tableOfLinks: TableOfLinks
    private val tableOfPhrases: TableOfPhrases
    private val tableOfCharacters: TableOfCharacters
    private val messageSideHandler: AdapterSideHandler

    init {
        tableOfLinks = TableOfLinks(activity, storyId, chapterCode, langCode, storyType)
        tableOfPhrases = TableOfPhrases(activity, storyId, chapterCode, langCode, storyType)
        tableOfCharacters = TableOfCharacters(activity, storyId, chapterCode, langCode, storyType)
        messageSideHandler = AdapterSideHandler(activity, storyId, true)
    }

    private fun isTooShortStory(): Boolean {
        return tableOfPhrases.array.size <= PHRASES_COUNT_MIN
    }

    fun setPhrases(phrases: ArrayList<Phrase>) {
        tableOfPhrases.array = phrases
    }

    fun setLinks(links: ArrayList<TableOfLinks.Link>) {
        tableOfLinks.links = links
    }

    fun setCharacters(characters: ArrayList<StoryCharacter>) {
        tableOfCharacters.characters = characters
    }

    fun setMessageSideHandler(sides: ArrayList<Int>) {
        messageSideHandler.left = sides
    }


    fun hasSame(characters: TableOfCharacters): Boolean {
        if (characters.characters.size != this.tableOfCharacters.characters.size) {
            return false
        }

        characters.characters.forEachIndexed { index, storyCharacter ->
            if (storyCharacter != this.tableOfCharacters.characters[index]) {
                return false
            }
        }
        return true
    }

    fun hasSame(phrases: TableOfPhrases): Boolean {
        if (phrases.array.size != this.tableOfPhrases.array.size) {
            return false
        }

        phrases.array.forEachIndexed { index, o ->
            if (o != this.tableOfPhrases.array[index]) {
                return false
            }
        }
        return true
    }

    fun hasSame(messageSideHandler: AdapterSideHandler): Boolean {
        if (messageSideHandler.left.size != this.messageSideHandler.left.size) {
            return false
        }

        messageSideHandler.left.forEachIndexed { index, i ->
            if (i != this.messageSideHandler.left[index]) {
                return false
            }
        }

        return true
    }

    fun hasSame(links: TableOfLinks): Boolean {
        if (links.links.size != this.tableOfLinks.links.size) {
            return false
        }

        links.links.forEachIndexed { index, o ->
            if (o != this.tableOfLinks.links[index]) {
                return false
            }
        }
        return true
    }

    /**
     * Check the bad words
     * @param onComplete Function1<[@kotlin.ParameterName] Int, Unit>
     */
    fun validateWordsQuality(onComplete: (suspiciousWordsLevel: Int) -> Unit) {
        val url = "https://data.sutoko.app/banned-words/${Language.determineLangDirectory()}"
        var processing = true
        try {
            url
                .httpGet().timeout(6000).responseString { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            processing = false
                            val json = result.get()
                            if (isJSONValid(json)) {
                                onComplete(controlBadWords(json))
                            } else {
                                onComplete(-1)
                            }
                        }
                        else -> {
                            if (processing) {
                                onComplete(-1)
                                processing = false
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            if (processing) {
                onComplete(-1)
                processing = false
            }
        }
    }

    /**
     * Returns the Links as a Json String
     * @return String
     */
    fun getLinksJson(): String {
        if (tableOfLinks.links.size == 0) {
            throw IllegalStateException("Array (links) size must be greater than 0 to upload it on the cloud.")
        }
        return Gson().toJson(tableOfLinks.links)
    }

    /**
     * Returns the Phrases as a Json String
     * @return String
     */
    fun getPhrasesJson(): String {
        if (tableOfPhrases.array.size == 0) {
            throw IllegalStateException("Array (phrases) size must be greater than 0 to upload it on the cloud.")
        }
        return Gson().toJson(tableOfPhrases.array)
    }

    /**
     * Returns the characters as a Json String
     * @return String
     */
    fun getCharactersJson(): String {
        if (tableOfCharacters.characters.size == 0) {
            throw IllegalStateException("Array (characters) size must be greater than 0 to upload it on the cloud.")
        }
        return Gson().toJson(tableOfCharacters.characters)
    }

    /**
     * Returns the characters as a Json String
     * @return String
     */
    fun getSideHandlerJson(): String {
        return Gson().toJson(messageSideHandler.left)
    }


    /**
     * Controls the bad words
     * @param badWordsJson String
     * @return Int
     */
    private fun controlBadWords(badWordsJson: String): Int {
        val gson = Gson()
        val type: Type = object :
            TypeToken<List<String>?>() {}.type
        val badWords: ArrayList<String> = gson.fromJson(badWordsJson, type)

        var occurrences = 0
        var text = StringBuilder()
        tableOfPhrases.array.forEach {
            text.append(it.sentence.lowercase(Locale.getDefault()))
            text.append(" ")
        }
        for (i in 0 until badWords.count()) {
            do {
                val word = badWords[i].lowercase(Locale.getDefault())
                val contains = text.contains(Regex("\\b$word\\b"))
                if (contains) {
                    text = StringBuilder(text.replaceFirst(Regex("\\b$word\\b"), ""))
                    occurrences++
                }

            } while (contains)
        }
        return occurrences
    }


    companion object {
        private const val PHRASES_COUNT_MIN = 6

        /**
         * Determines if the String is a valid json
         *
         * @param test
         * @return
         */
        private fun isJSONValid(test: String?): Boolean {
            if (null == test) {
                return false
            }

            try {
                JSONObject(test)
            } catch (ex: JSONException) {
                try {
                    JSONArray(test)
                } catch (ex1: JSONException) {
                    return false
                }
            }
            return true
        }

        fun getCharactersFromJson(json: String): List<StoryCharacter> {
            if (!isJSONValid(json)) {
                throw IllegalStateException("Invalid json . '$json'")
            }
            return Gson().fromJson<List<StoryCharacter>>(
                json,
                object : TypeToken<List<StoryCharacter>>() {}.type
            )
        }

        fun getLinksFromJson(json: String): List<TableOfLinks.Link> {
            if (!isJSONValid(json)) {
                throw IllegalStateException("Invalid json . '$json'")
            }
            return Gson().fromJson<List<TableOfLinks.Link>>(
                json,
                object : TypeToken<List<TableOfLinks.Link>>() {}.type
            )
        }

        fun getPhrasesFromJson(json: String): List<Phrase> {
            if (!isJSONValid(json)) {
                throw IllegalStateException("Invalid json . '$json'")
            }
            return Gson().fromJson<List<Phrase>>(json, object : TypeToken<List<Phrase>>() {}.type)
        }

        fun getMessageSideFromJson(json: String): List<Int> {
            if (!isJSONValid(json)) {
                throw IllegalStateException("Invalid json . '$json'")
            }
            return Gson().fromJson<List<Int>>(json, object : TypeToken<List<Int>>() {}.type)
        }
    }
}