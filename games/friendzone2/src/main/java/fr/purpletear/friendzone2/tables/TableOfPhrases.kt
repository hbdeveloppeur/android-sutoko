/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.tables

import android.content.Context
import com.example.sutokosharedelements.SmsGameTreeStructure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.purpletear.friendzone2.Data
import fr.purpletear.friendzone2.configs.Phrase
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Std
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.lang.IllegalStateException
import java.util.ArrayList

class TableOfPhrases {

    /**
     * List of array
     * @see Phrase
     */
    var array = ArrayList<Phrase>()
    private set

    /**
     * Reads the chapter's param file
     * @param c Context
     */
    @Throws(IOException::class)
    fun read(c: Context, chapterCode: String) {
        val file = SmsGameTreeStructure.getStoryPhrasesFile(c, GlobalData.Game.FRIENDZONE2.id, chapterCode, Language.determineLangDirectory())

        if (!file.exists()) {

            throw IllegalStateException("Couldn't find file ${file.path} for story number ${GlobalData.Game.FRIENDZONE2.id}")
        }

        var br: BufferedReader? = null
        try {
            br = BufferedReader(FileReader(file))

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (br != null) {
            val listType = object : TypeToken<List<Phrase>>() {}.type
            val newList = Gson().fromJson<List<Phrase>>(br, listType)
            array = ArrayList(newList)
            br.close()
        }
    }

    /**
     * Reads the chapter's param file
     * @param c Context
     */
    @Throws(IOException::class)
    fun readEvaPhoneConversation(c: Context, chapterCode: String) {
        read(c, chapterCode)
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
        throw IllegalArgumentException()
    }

    /**
     * Describes the table
     * @see Phrase
     */
    fun describe() {
        Std.debug("***** DESCRIBE TABLE OF PHRASES *****")
        for (p in array) {
            Std.debug(p.toString())
        }
        Std.debug("***********************************")
    }

    /**
     * Returns the size of the table
     * @return int
     */
    fun count(): Int {
        return array.size
    }

    /**
     * Returns the chapter array filename
     * @return String
     */
    private fun getChapterPhrasesFileName(chapterCode: String): String {
        return chapterCode + "_phrases.json"
    }
}
