package fr.purpletear.friendzone4.game.tables

import android.content.Context
import com.example.sharedelements.SmsGameTreeStructure

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.io.IOException
import java.util.ArrayList

import fr.purpletear.friendzone4.game.activities.main.Phrase
import purpletear.fr.purpleteartools.GameLanguage
import purpletear.fr.purpleteartools.GlobalData
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader

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
        val file = SmsGameTreeStructure.getStoryPhrasesFile(c, GlobalData.Game.FRIENDZONE4.id.toString(), chapterCode, GameLanguage.determineLangDirectory())

        if (!file.exists()) {
            throw IllegalStateException("Couldn't find file ${file.path} for story number ${GlobalData.Game.FRIENDZONE4.id.toString()}")
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
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
     * Returns the size of the table
     * @return int
     */
    fun count(): Int {
        return array.size
    }


}
