@file:Suppress("SpellCheckingInspection")

package com.purpletear.smsgame.activities.smsgame.tables

import android.app.Activity
import com.google.gson.Gson
import java.io.*
import java.lang.IllegalStateException
import java.util.ArrayList
import com.google.gson.reflect.TypeToken
import com.example.sharedelements.SmsGameTreeStructure
import com.example.sharedelements.SmsGameTreeStructure.Companion.userStorCharactersFileName
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import purpletear.fr.purpleteartools.CFiles

/**
 * A TableOfCharacters contains all the Story's Characters
 * @param activity: Activity
 * @param storyId : Int
 * @author Hocine Belbouab <hbdeveloppeur@gmail.com>
 */
class TableOfCharacters(
    activity: Activity?,
    val storyId: String,
    chapterCode: String,
    langCode: String,
    storyType: StoryType = StoryType.OFFICIAL_STORY
) {
    /**
     * Contains the list of characters of the story
     */
    var characters: ArrayList<StoryCharacter> = ArrayList()
    var isValid: Boolean = true


    init {
        if (storyType != StoryType.OTHER_USER_STORY) {
            try {
                read(activity!!, storyId, chapterCode, langCode, storyType)
            } catch (e: Exception) {
                this.isValid = false
            }
        }
    }

    constructor(chapterCode: String, langCode: String, storyType: StoryType) : this(
        null,
        "-1",
        chapterCode,
        langCode,
        storyType
    )

    /**
     * Reads the Story's characters table
     * @param activity Activity
     * @param storyId String
     * @param chapterCode String
     * @param langCode String
     * @param storyType StoryType
     */
    @Throws(Exception::class)
    private fun read(
        activity: Activity,
        storyId: String,
        chapterCode: String,
        langCode: String,
        storyType: StoryType
    ) {
        val file = when (storyType) {
            StoryType.OFFICIAL_STORY -> SmsGameTreeStructure.getStoryCharactersFile(
                activity,
                storyId,
                chapterCode,
                langCode
            )
            StoryType.CURRENT_USER_STORY -> SmsGameTreeStructure.getUserStoryCharactersFile(
                activity,
                storyId
            )
            else -> return
        }

        var br: BufferedReader? = CFiles.read(activity, file.parent ?: "", file.name)

        if (storyType == StoryType.OFFICIAL_STORY) {
            br = CFiles.read(activity, file.parent ?: "", file.name, false)
        }


        characters = if (br != null) {
            val listType = object : TypeToken<List<StoryCharacter>>() {}.type
            val newList = Gson().fromJson<List<StoryCharacter>>(br, listType)
            br.close()
            ArrayList(newList)
        } else {
            if (storyType == StoryType.OFFICIAL_STORY) {
                throw IllegalStateException("Couldn't find file ${file.absolutePath} for story number $storyId")
            }

            ArrayList()
        }
    }

    fun edit(character: StoryCharacter) {
        val position = characters.indexOf(character)
        if (position == -1) {
            return
        }
        characters[position] = character
    }


    fun getAvailableCharacterId(id: Int = 1): Int {
        if (characters.size == 0) {
            return 1
        }

        for (character in characters) {
            if (character.id == id) {
                return getAvailableCharacterId(id + 1)
            }
        }
        return id
    }


    fun getFirstAvailableCharacterId(id: Int = 1): Int {
        if (characters.size == 0) {
            return 1
        }

        for (character in characters) {
            if (character.id == id) {
                return id
            }
        }
        return getAvailableCharacterId(id + 1)
    }


    fun addCharacter(character: StoryCharacter) {
        characters.add(character)
    }

    fun save(activity: Activity): Boolean {
        return CFiles.save(
            activity,
            SmsGameTreeStructure.getUserStoryFile(activity, storyId).absolutePath,
            userStorCharactersFileName,
            characters
        )
    }

    /**
     * Returns a Character given an Id
     * @param id : Int
     * @return Character
     */
    fun getCharacter(id: Int): StoryCharacter {
        for (character in characters) {
            if (character.id == id) {
                return character
            }
        }
        if (id == 0) {
            return StoryCharacter(0, "", "", true)
        }
        throw IllegalStateException("Couldn't find the character. {storyId:$storyId, characterId:$id}")
    }

    fun getNextCharacter(fromCharacter: StoryCharacter): StoryCharacter {
        val index = characters.indexOf(fromCharacter)
        var cursor = index + 1
        if (characters.size == cursor) {
            cursor = 0
        }
        return characters[cursor]
    }

}
