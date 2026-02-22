/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */


package com.purpletear.smsgame.activities.smsgame.tables

import android.app.Activity
import androidx.annotation.Keep
import com.example.sharedelements.SmsGameTreeStructure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import purpletear.fr.purpleteartools.CFiles
import purpletear.fr.purpleteartools.Language
import java.io.BufferedReader
import java.io.FileNotFoundException

@Keep
class TableOfPhrases(
    activity: Activity?,
    private var storyId: String?,
    chapterCode: String,
    langCode: String,
    storyType: StoryType = StoryType.OFFICIAL_STORY
) {

    /**
     * List of array
     * @see Phrase
     */
    var array = ArrayList<Phrase>()

    init {
        if (null != storyId && storyType != StoryType.OTHER_USER_STORY) {
            read(activity!!, storyId!!, chapterCode, langCode, storyType)
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
     * Reads the Story's file that contains all Phrase
     * @param context : Context
     * @param storyId : String
     */
    private fun read(
        activity: Activity,
        storyId: String,
        chapterCode: String,
        langCode: String,
        isUserStory: StoryType = StoryType.OFFICIAL_STORY
    ) {

        val file = when (isUserStory) {
            StoryType.OFFICIAL_STORY -> SmsGameTreeStructure.getStoryPhrasesFile(
                activity,
                storyId,
                chapterCode,
                langCode
            )

            StoryType.CURRENT_USER_STORY -> SmsGameTreeStructure.getUserStoryPhrasesFile(
                activity,
                storyId
            )

            StoryType.OTHER_USER_STORY -> return
        }

        var br: BufferedReader? = CFiles.read(activity, file.parent ?: "", file.name)
        if (isUserStory == StoryType.OFFICIAL_STORY) {
            br = CFiles.read(activity, file.parent ?: "", file.name, false)
        }
        if (br != null) {
            val listType = object : TypeToken<List<Phrase>>() {}.type
            val newList = Gson().fromJson<List<Phrase>>(br, listType)
            array = ArrayList(newList)
            br.close()
        } else {
            if (isUserStory == StoryType.OFFICIAL_STORY) {
                throw FileNotFoundException()
            }

            array = ArrayList()
        }
    }

    fun save(activity: Activity): Boolean {
        return CFiles.save(
            activity, SmsGameTreeStructure.getUserStoryFile(activity, storyId!!).path,
            SmsGameTreeStructure.userStoryPhrasesFileName, array
        )
    }


    /**
     * Returns the Phrases given an ArrayList of ids
     * @param ids : ArrayList<Int>
     * @return ArrayList<Phrase>
     */
    fun getPhrases(ids: ArrayList<Int>): ArrayList<Phrase> {
        val newArrayList: ArrayList<Phrase> = ArrayList()
        ids.forEach {
            newArrayList.add(getPhrase(it))
        }
        return newArrayList
    }

    fun hasId(id: Int): Boolean {
        for (phrase in array) {
            if (phrase.id == id) {
                return true
            }
        }
        return false
    }

    /**
     * Finds a phrase by its id
     * @param byId int phrase's id
     * @return Phrase
     */
    fun getPhrase(byId: Int): Phrase {
        for (phrase in array) {
            if (phrase.id == byId) {
                return phrase
            }
        }
        throw IllegalStateException("Tried to find a Phrase with id $byId in story with story id $storyId but didn't find it. (langCode : ${Language.determineLangDirectory()})")
    }
}
