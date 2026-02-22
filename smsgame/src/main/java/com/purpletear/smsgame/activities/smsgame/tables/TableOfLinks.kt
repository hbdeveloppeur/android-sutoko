/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.purpletear.smsgame.activities.smsgame.tables

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.SmsGameTreeStructure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import purpletear.fr.purpleteartools.CFiles
import java.io.BufferedReader
import java.io.FileNotFoundException

@Keep
class TableOfLinks(
    activity: Activity?,
    storyId: String?,
    chapterCode: String,
    langCode: String,
    storyType: StoryType = StoryType.OFFICIAL_STORY
) {

    /**
     * Contains the links between array
     */
    var links = ArrayList<Link>()


    init {
        if (storyType != StoryType.OTHER_USER_STORY && storyId != null) {
            read(activity!!, storyId, chapterCode, langCode, storyType)
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
     * Reads the chapter's param file
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
        storyType: StoryType = StoryType.OFFICIAL_STORY
    ) {

        val file = when (storyType) {
            StoryType.OFFICIAL_STORY -> SmsGameTreeStructure.getStoryLinksFile(
                activity,
                storyId,
                chapterCode,
                langCode
            )

            StoryType.CURRENT_USER_STORY -> SmsGameTreeStructure.getUserStoryLinksFile(
                activity,
                storyId
            )

            else -> return
        }

        var br: BufferedReader? = CFiles.read(activity, file.parent ?: "", file.name)
        if (storyType == StoryType.OFFICIAL_STORY) {
            br = CFiles.read(activity, file.parent ?: "", file.name, false)
        }
        if (br != null) {
            val listType = object : TypeToken<List<Link>>() {}.type
            val newList = Gson().fromJson<List<Link>>(br, listType)
            links = ArrayList(newList)
            br.close()
        } else {
            if (storyType == StoryType.OFFICIAL_STORY) {
                throw FileNotFoundException()
            }

            links = ArrayList()
        }
    }


    fun save(activity: Activity, storyId: String): Boolean {
        return CFiles.save(
            activity, SmsGameTreeStructure.getUserStoryFile(activity, storyId).absolutePath,
            SmsGameTreeStructure.userStoryLinksFileName, links
        )
    }

    /**
     * A Link represent the connection between two array
     */
    @Keep
    class Link internal constructor(
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

        internal val c: Int
    ) : Parcelable {


        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<Link> {
                override fun createFromParcel(parcel: Parcel) = Link(parcel)
                override fun newArray(size: Int) = arrayOfNulls<Link>(size)
            }
        }

        init {}

        private constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(src)
            parcel.writeString(dest)
            parcel.writeInt(c)
        }

        override fun describeContents() = 0


        override fun toString(): String {
            return "Link(src='$src', dest='$dest', c=$c)"
        }
    }
}


