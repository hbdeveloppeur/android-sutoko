/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.tables

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.purpletear.friendzone.Data
import fr.purpletear.friendzone.config.Phrase
import purpletear.fr.purpleteartools.Std
import java.io.IOException
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
        val path = Data.getPhrasesPath(chapterCode, Language.determineLangDirectory())
        val content = Data.getAssetContent(c.assets, path)

        val gson = Gson()
        array = gson.fromJson<ArrayList<Phrase>>(content, object : TypeToken<List<Phrase>>() {

        }.type)
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
