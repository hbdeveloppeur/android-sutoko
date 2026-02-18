package com.example.sharedelements.tables.trophies

import android.app.Activity
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.SutokoPlayerPointsManager
import com.example.sharedelements.SutokoSharedElementsData
import com.example.sharedelements.tables.points.PlayerPoint
import com.google.gson.Gson
import purpletear.fr.purpleteartools.CFiles

/**
 * This table contains the collected trophies.
 */
@Keep
class TableOfCollectedTrophies {
    private var array: ArrayList<CollectedTrophyLog> = ArrayList()
    private var playerState: SutokoPlayerPointsManager = SutokoPlayerPointsManager()

    /**
     * Adds a TrophyLog to the list
     *
     * @param trophy
     */
    fun add(activity: Activity, trophy: Trophy, apiVersion: Int) {
        add(activity, trophy.id, trophy.storyId, apiVersion)
    }

    /**
     * Adds a TrophyLog to the list
     *
     * @param trophyId
     * @param trophyStoryId
     * @param apiVersion
     */
    fun add(activity: Activity, trophyId: Int, trophyStoryId: Int, apiVersion: Int) {
        val collectedTrophyLog =
            CollectedTrophyLog(
                trophyId,
                trophyStoryId,
                System.currentTimeMillis(),
                apiVersion
            )
        array.add(collectedTrophyLog)
        val point =
            PlayerPoint(PlayerPoint.PointType.TROPHY, 20, System.currentTimeMillis(), trophyId)
        playerState.addPointToLocal(activity, point, false)
    }

    /**
     * Clears the trophies list.
     * @param context
     */
    fun removeAll(context: Context) {
        array.clear()
    }

    /**
     * Determines if the table contains the given trophy id.
     * @param trophy
     * @return
     */
    fun contains(trophy: Trophy): Boolean {
        return containsByTrophyId(trophy.id)
    }

    /**
     * Determines if the table contains the given trophy id.
     * @param trophyId
     * @return
     */
    fun containsByTrophyId(trophyId: Int): Boolean {
        array.forEach {
            if (it.trophyId == trophyId) {
                return true
            }
        }
        return false
    }

    /**
     * Reads the table from the default
     *
     * @param activity
     */
    fun read(activity: Activity) {
        val br = CFiles.read(
            activity,
            SutokoSharedElementsData.getCollectedTrophiesDirPath(),
            SutokoSharedElementsData.FILE_NAME_TROPHIES
        )
        if (br == null) {
            array = ArrayList()
            return
        }
        val gson = Gson()
        val o = gson.fromJson(br, TableOfCollectedTrophies::class.java)
        array = o.array
        br.close()
    }

    /**
     * Saves the list in the default file
     *
     * @param activity
     */
    fun save(activity: Activity) {
        CFiles.save(
            activity,
            SutokoSharedElementsData.getCollectedTrophiesDirPath(),
            SutokoSharedElementsData.FILE_NAME_TROPHIES,
            this
        )
    }

    /**
     * Returns the number of collected trophies for the given story's id
     *
     * @param storyId
     * @return
     */
    fun countForStory(storyId: Int, displayedTrophies: ArrayList<Trophy>): Int {
        val counted = ArrayList<Int>()
        array.forEach {
            val trophy = Trophy(it.trophyId, it.storyId, "", "", "", true)
            if (it.storyId == storyId && !counted.contains(it.trophyId) && displayedTrophies.contains(
                    trophy
                )
            ) {
                counted.add(it.trophyId)
            }
        }
        return counted.size
    }

    /**
     * Returns the number of collected trophies for the given story's id
     *
     * @param storyId
     * @return
     */
    fun countForAllStories(): Int {
        val counted = ArrayList<Int>()
        array.forEach {
            if (!counted.contains(it.trophyId)) {
                counted.add(it.trophyId)
            }
        }
        return counted.size
    }

}

@Keep
private class CollectedTrophyLog(
    var trophyId: Int,
    var storyId: Int,
    var timeStamp: Long,
    var apiVersion: Int
) : Parcelable {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CollectedTrophyLog> =
            object : Parcelable.Creator<CollectedTrophyLog> {
                override fun createFromParcel(`in`: Parcel): CollectedTrophyLog {
                    return CollectedTrophyLog(
                        `in`
                    )
                }

                override fun newArray(size: Int): Array<CollectedTrophyLog?> {
                    return arrayOfNulls(size)
                }
            }
    }

    override fun describeContents(): Int {
        return 0
    }

    protected constructor(`in`: Parcel) : this(-1, -1, -1, -1) {
        trophyId = `in`.readInt()
        storyId = `in`.readInt()
        timeStamp = `in`.readLong()
        apiVersion = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(trophyId)
        dest.writeInt(storyId)
        dest.writeLong(timeStamp)
        dest.writeInt(apiVersion)
    }
}