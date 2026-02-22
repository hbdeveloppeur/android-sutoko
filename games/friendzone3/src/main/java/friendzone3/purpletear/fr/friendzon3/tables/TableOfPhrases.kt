/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package friendzone3.purpletear.fr.friendzon3.tables;

import android.content.Context
import com.example.sharedelements.SmsGameTreeStructure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import friendzone3.purpletear.fr.friendzon3.Data
import friendzone3.purpletear.fr.friendzon3.config.Language
import friendzone3.purpletear.fr.friendzon3.config.Var
import friendzone3.purpletear.fr.friendzon3.custom.Phrase
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
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
    fun read(c: Context, chapterCode: String, storyVersion : String) {
        val file = SmsGameTreeStructure.getStoryPhrasesFile(c, GlobalData.Game.FRIENDZONE3.id.toString(), chapterCode, Language.determineLangDirectory())

        if (!file.exists()) {
            throw IllegalStateException("Couldn't find file ${file.path} for story number ${GlobalData.Game.FRIENDZONE3.id.toString()}")
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

    companion object {

        /**
         * Determines the result of the given CONDITION
         * @param symbols : TableOfSymbols
         * @param values : Array<String?>
         * @return Phrase
         */
        public fun determineConditionResult(symbols: TableOfSymbols, values: Array<String?>, phrases : TableOfPhrases): Phrase? {
            val condition = values[0]!!.replace("[", "").replace("]", "").replace(" ", "").split("==".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val mThen = Integer.parseInt(values[1]!!)
            val mElse = Integer.parseInt(values[2]!!)

            val v = Var(condition[0], condition[1], -1)
            if (symbols.condition(159, v.name, v.value)) {
                if(mThen == 0) {
                    return null
                }
                if(mThen == 130) {
                    return null
                }
                return phrases.getPhrase(mThen)
            } else {
                if(mElse == 0) {
                    return null
                }
                return phrases.getPhrase(mElse)
            }
        }
    }
}
