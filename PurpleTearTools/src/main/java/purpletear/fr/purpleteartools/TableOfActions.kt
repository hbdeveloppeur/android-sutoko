package purpletear.fr.purpleteartools

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Contains the Actions the games requires
 * An Action has the following fields:
 * - storyId : Int
 * - chapterCode : String
 * - atTime : String (TimeStamp the Action should be executed)
 */
class TableOfActions : Serializable, Parcelable {
    private var array : ArrayList<Action> = ArrayList()
    private val SYMBOLS_DIR_PATH = "actions/"
    private val SYMBOLS_FILE_NAME = "actions.json"

    /**
     * Add or set
     *
     * @param storyId Int
     * @param chapterCode String
     * @param atTime Long
     */
    fun addOrSet(storyId: Int, chapterCode: String, atTime: Long) {
        remove(storyId)
        array.add(Action(storyId, chapterCode, atTime))
    }

    /**
     * Removes a row given the story id
     *
     * @param storyId Int
     */
    fun remove(storyId: Int) {
        if(array.contains(Action(storyId))) {
            array.remove(Action(storyId))
        }
    }

    /**
     * Saves the table in a file
     *
     * @param activity
     * @return
     */
    fun save(activity : Activity) : Boolean {
        return CFiles.save(activity, SYMBOLS_DIR_PATH, SYMBOLS_FILE_NAME, this)
    }

    /**
     * Reads the file
     *
     * @param activity
     */
    fun read(activity : Activity) {
        val br = CFiles.read(activity, SYMBOLS_DIR_PATH, SYMBOLS_FILE_NAME)
        val gson = Gson()
        val o = gson.fromJson(br, TableOfActions::class.java)
        br?.close()
        array = o.array
    }

    /**
     * Executes a fun if an Action should be executed in the table
     *
     * @param activity
     * @param executeFun
     */
    fun execute(activity : Activity, executeFun : (storyId : Int, chapterCode : String) -> Unit) {
        var found = false
        array.forEach {
            if(it.shouldExecute()) {
                Handler(Looper.getMainLooper()).post{
                    executeFun(it.storyId, it.chapterCode)
                }
                found = true
            }
        }
        if(found) {
            save(activity)
        }
    }

    protected constructor(`in`: Parcel) {
        `in`.readTypedList(array, Action.CREATOR)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TableOfActions> = object : Parcelable.Creator<TableOfActions> {
            override fun createFromParcel(`in`: Parcel): TableOfActions {
                return TableOfActions(`in`)
            }

            override fun newArray(size: Int): Array<TableOfActions?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(array)
    }
}

private class Action(var storyId : Int, var chapterCode : String, var atTime : Long) : Parcelable {

    constructor(storyId: Int) : this(storyId, "", -1) {
        this.storyId = storyId
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Action

        if (storyId != other.storyId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = storyId
        result = 31 * result + chapterCode.hashCode()
        return result
    }

    fun shouldExecute() : Boolean {
        return Calendar.getInstance().timeInMillis >= atTime
    }


    protected constructor(`in`: Parcel) : this(-1, "", -1) {
        this.storyId = `in`.readInt()
        this.chapterCode = `in`.readString() ?: ""
        this.atTime = `in`.readLong()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Action> = object : Parcelable.Creator<Action> {
            override fun createFromParcel(`in`: Parcel): Action {
                return Action(`in`)
            }

            override fun newArray(size: Int): Array<Action?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(storyId)
        dest.writeString(chapterCode)
        dest.writeLong(atTime)
    }
}