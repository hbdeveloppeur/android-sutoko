package com.purpletear.smsgame.activities.smsgame.tables

import android.app.Activity
import androidx.annotation.Keep
import com.example.sutokosharedelements.SmsGameTreeStructure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.smsgame.activities.smsgame.objects.Story
import purpletear.fr.purpleteartools.CFiles
import java.io.BufferedReader
import java.io.File


/**
 * Contains the list of Story
 * @see fr.purpletear.sutoko.screens.smsgame.objects.Story
 * @property array ArrayList<Story>
 * @constructor
 */
@Keep
class TableOfStories(activity: Activity) {
    var array: ArrayList<Story> = ArrayList()
        private set

    init {
        read(activity)
    }

    fun add(story: Story) {
        array.add(story)

    }

    fun remove(activity: Activity, story: Story) {
        array.remove(story)
        File(
            activity.filesDir,
            SmsGameTreeStructure.getUserStoryFile(activity, story.id).path
        ).deleteDirectory()
    }

    fun removeAll(activity: Activity) {
        array.forEach { story ->
            File(
                activity.filesDir,
                SmsGameTreeStructure.getUserStoryFile(activity, story.id).path
            ).deleteDirectory()
        }
    }


    private fun File.deleteDirectory(): Boolean {
        return if (exists()) {
            listFiles()?.forEach {
                if (it.isDirectory) {
                    it.deleteDirectory()
                } else {
                    it.delete()
                }
            }
            delete()
        } else false
    }

    /**
     * Generates an available id
     * @param id Int
     * @return Int
     */
    fun generateStoryId(id: Int = 1): Int {
        array.forEach { story ->
            if (id == story.id) {
                return generateStoryId(id + 1)
            }
        }
        return id
    }

    /**
     * Sets the Firebase story id
     * @param id String
     * @param story Story
     */
    fun setFirebaseStoryId(id: String, story: Story) {
        val position = indexOf(story)

        array[position].firebaseId = id
    }

    fun indexOf(story: Story): Int {
        array.forEachIndexed { index, s ->
            if (story.id == s.id && story.title == s.title) {
                return index
            }
        }
        return -1
    }

    private fun read(activity: Activity) {

        val br: BufferedReader? = CFiles.read(
            activity,
            SmsGameTreeStructure.getUserStoriesDirectoryPath(activity),
            SmsGameTreeStructure.userStoriesFileName
        )

        array = if (br != null) {
            val listType = object : TypeToken<List<Story>>() {}.type
            val newList = Gson().fromJson<List<Story>>(br, listType)
            br.close()
            ArrayList(newList)
        } else {
            ArrayList()
        }
    }

    fun save(activity: Activity): Boolean {
        return CFiles.save(
            activity, SmsGameTreeStructure.getUserStoriesDirectoryPath(activity),
            SmsGameTreeStructure.userStoriesFileName, array
        )
    }
}