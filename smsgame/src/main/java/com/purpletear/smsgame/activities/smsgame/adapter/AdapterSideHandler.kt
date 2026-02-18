package com.purpletear.smsgame.activities.smsgame.adapter

import android.app.Activity
import com.example.sutokosharedelements.SmsGameTreeStructure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import purpletear.fr.purpleteartools.CFiles
import java.io.BufferedReader

class AdapterSideHandler(activity: Activity, storyId: Int, isUserStory: Boolean) {
    var left: ArrayList<Int> = ArrayList()


    init {
        if (isUserStory) {
            read(activity, storyId)
        } else {
            left = ArrayList()
        }
    }

    fun toLeft(id: Int) {
        left.remove(id)
        left.add(id)
    }

    fun toRight(id: Int) {
        left.remove(id)
    }

    fun hasOnLeft(id: Int): Boolean {
        return left.contains(id)
    }

    /**
     * Reads the Story's file that contains all Phrase
     * @param context : Context
     * @param storyId : Int
     */
    private fun read(activity: Activity, storyId: Int) {
        val file = SmsGameTreeStructure.getUserStorySideHandlerFile(activity, storyId)

        val br: BufferedReader? = CFiles.read(activity, file.parent ?: "", file.name)

        if (br != null) {
            val listType = object : TypeToken<List<Int>>() {}.type
            val newList = Gson().fromJson<List<Int>>(br, listType)
            left = java.util.ArrayList(newList)
            br.close()
        } else {
            left = java.util.ArrayList()
        }
    }

    fun save(activity: Activity, storyId: Int): Boolean {
        return CFiles.save(
            activity, SmsGameTreeStructure.getUserStoryFile(activity, storyId).path,
            SmsGameTreeStructure.userStorySideHandlerFileName, left
        )
    }

}