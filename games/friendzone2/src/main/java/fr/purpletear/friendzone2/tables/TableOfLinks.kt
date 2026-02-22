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
import fr.purpletear.friendzone2.configs.Phrase
import com.google.gson.reflect.TypeToken
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Std
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.lang.IllegalStateException

class TableOfLinks {

    /**
     * Contains the links between array
     */
    private var links = ArrayList<Link>()

    /**
     * Adds a link to the list.
     *
     * @param srcId  int source's id
     * @param destId int dest's id
     * @see Link
     */
    fun append(srcId: String, destId: String, c : Int) {
        links.add(Link(srcId, destId, c))
    }

    /**
     * Returns the number of links.
     *
     * @return number of links
     */
    fun size(): Int {
        return links.size
    }

    /**
     * Returns an arrayList of the dest array id
     *
     * @param srcId source's id
     * @return ArrayList
     */
    fun getDest(srcId: Int): ArrayList<Int> {
        val ids = ArrayList<Int>()
        for (link in links) {
            if (Integer.valueOf(link.src) == srcId) {
                ids.add(Integer.valueOf(link.dest))
            }
        }
        return ids
    }

    /**
     * Returns an arrayList of the dest array id
     *
     * @param srcId source's id
     * @return ArrayList
     */
    fun getDestPhrases(srcId: Int, tableOfPhrases: TableOfPhrases): ArrayList<Phrase> {
        val phrases = ArrayList<Phrase>()
        for (link in links) {
            if (Integer.valueOf(link.src) == srcId) {
                val id = link.dest
                phrases.add(tableOfPhrases.getPhrase(Integer.valueOf(id)))
            }
        }
        return phrases
    }

    /**
     * Determines if the source phrase has an answer (a next)
     *
     * @param srcId: String source's id
     * @return boolean
     */
    fun hasAnswer(srcId: String): Boolean {
        for (link in links) {
            if (link.src.equals(srcId)) {
                return true
            }
        }
        return false
    }

    /**
     * Determines if the source phrase has an answer (a next)
     *
     * @param srcId: Int source's id
     * @return boolean
     */
    fun hasAnswer(srcId: Int) : Boolean {
        return hasAnswer(srcId.toString())
    }

    /**
     * Determines if the next Phrase is a choice of the user
     *
     * @param srcId          source's id
     * @param tableOfPhrases Table of array
     * @return boolean
     */
    fun answerIsUserChoice(srcId: String, tableOfPhrases: TableOfPhrases): Boolean {
        for (link in links) {
            if (link.src.equals(srcId) && tableOfPhrases.getPhrase(Integer.valueOf(link.dest)).id_author == 0) {
                return true
            }
        }
        return false
    }

    /**
     * Reads the chapter's param file
     *
     * @param c Context
     */
    @Throws(IOException::class)
    fun read(c: Context, chapterCode: String) {
        val file = SmsGameTreeStructure.getStoryLinksFile(c, GlobalData.Game.FRIENDZONE2.id.toString(), chapterCode, Language.determineLangDirectory())

        if (!file.exists()) {
            throw IllegalStateException("Couldn't find file ${file.path} for story number ${GlobalData.Game.FRIENDZONE2.id.toString()}")
        }

        var br: BufferedReader? = null
        try {
            br = BufferedReader(FileReader(file))

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (br != null) {
            val listType = object : TypeToken<List<Link>>() {}.type
            val newList = Gson().fromJson<List<Link>>(br, listType)
            links = java.util.ArrayList(newList)
            br.close()
        }
    }

    /**
     * Reads the chapter's param file
     *
     * @param c Context
     */
    @Throws(IOException::class)
    fun readEvaPhoneConversation(c: Context, chapterCode: String) {
        read(c, chapterCode)
    }


    /**
     * Describes the table
     */
    fun describe() {
        Std.debug("***** DESCRIBE TABLE OF LINKS *****")
        for (link in links) {
            Std.debug(link.toString())
        }
        Std.debug("***********************************")
    }


    /**
     * Returns the name of the file containing the links.
     *
     * @return String
     */
    private fun getChapterLinksFileName(chapterCode: String): String {
        return chapterCode + "_links.json"
    }

    /**
     * A Link represent the connection between two array
     */
    private inner class Link internal constructor(
            /**
             * The id of the incoming phrase
             */
            /**
             * The source id
             *
             * @return int
             */
            internal val src: String,
            /**
             * The id of the next phrase
             */
            /**
             * The dest id
             *
             * @return int
             */
            internal val dest: String,

            internal val c: Int) {
        override fun toString(): String {
            return "Link(src='$src', dest='$dest', c=$c)"
        }
    }
}


