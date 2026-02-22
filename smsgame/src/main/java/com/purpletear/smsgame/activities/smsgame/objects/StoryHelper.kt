package com.purpletear.smsgame.activities.smsgame.objects

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.sharedelements.Data
import purpletear.fr.purpleteartools.CFiles
import purpletear.fr.purpleteartools.Language
import purpletear.fr.purpleteartools.Std

@Keep

object StoryHelper {

    private const val filename: String = "story_activity.json"
    private var likedStories: ArrayList<StatRecord> = ArrayList()
    private var readStories: ArrayList<StatRecord> = ArrayList()

    fun save(activity: Activity) {
        CFiles.save(activity, "", filename, this)
    }

    fun read(activity: Activity) {
        val br = CFiles.read(activity, "", filename) ?: return
        val listType = object : TypeToken<StoryHelper>() {}.type
        val tmp = Gson().fromJson<StoryHelper>(br, listType)
        likedStories = tmp.likedStories
        readStories = tmp.readStories
        br.close()
    }

    private fun canAddRead(storyId: String): Boolean {
        readStories.forEach {
            if (it.storyId == storyId) {
                return it.timeStamp < (System.currentTimeMillis() - 60 * 1000)
            }
        }
        return true
    }

    fun addRead(story: Story, onSuccess: () -> Unit = {}) {
        if (canAddRead(story.id)) {
            incrementValueOnline(story, "reads", onSuccess)
            readStories.add(StatRecord(story.id))
        }
    }

    private fun canAddLike(storyId: String): Boolean {
        likedStories.forEach {
            if (it.storyId == storyId) {
                return false
            }
        }
        return true
    }

    fun userLiked(storyId: String): Boolean {
        likedStories.forEach {
            if (it.storyId == storyId) {
                return true
            }
        }
        return false
    }

    fun addLike(story: Story, onSuccess: () -> Unit) {
        if (canAddLike(story.id)) {
            incrementValueOnline(story, "likes", onSuccess)
            likedStories.add(StatRecord(story.id))
        }
    }

    fun removeLike(story: Story, onSuccess: () -> Unit) {
        if (!canAddLike(story.id)) {
            decrementValueOnline(story, "likes", onSuccess)
            likedStories.remove(StatRecord(story.id))
        }
    }

    private fun incrementValueOnline(story: Story, field: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentRef = db
            .collection("${Data.FIREBASE_COLLECTION_USTORIES}/${Language.determineLangDirectory()}/${Data.FIREBASE_COLLECTION_STORIES}")
            .document(story.firebaseId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)

            val v: Int = (snapshot.getDouble(field) ?: 0).toInt()
            val newNumber = v + 1
            transaction.update(documentRef, field, newNumber)

            null
        }
            .addOnSuccessListener {
                Handler(Looper.getMainLooper()).post { onSuccess() }
            }
            .addOnFailureListener { e ->
                Std.debug(e)
            }
    }

    private fun decrementValueOnline(story: Story, field: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentRef = db
            .collection("${Data.FIREBASE_COLLECTION_USTORIES}/${Language.determineLangDirectory()}/${Data.FIREBASE_COLLECTION_STORIES}")
            .document(story.firebaseId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)

            val v: Int = (snapshot.getDouble(field) ?: 0).toInt()
            val newNumber = v - 1
            transaction.update(documentRef, field, newNumber)

            null
        }
            .addOnSuccessListener {
                Handler(Looper.getMainLooper()).post { onSuccess() }
            }
            .addOnFailureListener { e ->
                Std.debug(e)
            }
    }
}

class StatRecord(
    var storyId: String,
    var timeStamp: Long = System.currentTimeMillis()
) : Parcelable {
    protected constructor(`in`: Parcel) : this("-1", -1) {
        this.storyId = `in`.readString() ?: "-1"
        this.timeStamp = `in`.readLong()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StatRecord> = object : Parcelable.Creator<StatRecord> {
            override fun createFromParcel(`in`: Parcel): StatRecord {
                return StatRecord(`in`)
            }

            override fun newArray(size: Int): Array<StatRecord?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(storyId)
        dest.writeLong(timeStamp)
    }

    override fun equals(other: Any?): Boolean {
        return other is StatRecord && other.storyId == this.storyId
    }

    override fun hashCode(): Int {
        var result = storyId.hashCode()
        result = 31 * result + timeStamp.hashCode()
        return result
    }
}