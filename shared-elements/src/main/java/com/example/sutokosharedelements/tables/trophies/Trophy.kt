package com.example.sharedelements.tables.trophies

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.SutokoSharedElementsData
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
class Trophy(
    id: Int,
    storyId: Int,
    title: String,
    description: String,
    shortDescription: String,
    isUnlocked: Boolean
) : TrophiesRecyclerViewItem, Serializable, Parcelable {

    @PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_ID)
    @get:PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_ID)
    @SerializedName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_ID)
    var id: Int = id
        private set

    @PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_STORY_ID)
    @get:PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_STORY_ID)
    @SerializedName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_STORY_ID)
    var storyId: Int = storyId
        private set

    @PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_TITLE)
    @get:PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_TITLE)
    @SerializedName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_TITLE)
    var title: String = title
        private set

    @PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_DESCRIPTION)
    @get:PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_DESCRIPTION)
    @SerializedName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_DESCRIPTION)
    var description: String = description
        private set

    @PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_SHORT_DESCRIPTION)
    @get:PropertyName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_SHORT_DESCRIPTION)
    @SerializedName(SutokoSharedElementsData.FIREBASE_TROPHY_KEY_SHORT_DESCRIPTION)
    var shortDescription: String = shortDescription
        private set

    @Exclude
    var isUnlocked: Boolean = isUnlocked
        private set

    // Required by Firebase Firestore
    @Suppress("unused")
    constructor() : this(-1, -1, "", "", "", false)


    companion object {

        /**
         * Returns a News array from a QuerySnapshot
         * @param news : QuerySnapshot
         * @return ArrayList<News>
         */
        fun getTrophiesFromFirebaseDocumentSnapshot(trophies: QuerySnapshot): ArrayList<Trophy> {
            val array = ArrayList<Trophy>()
            for (snapshot in trophies) {
                array.add(snapshot.toObject(Trophy::class.java))
            }
            return array
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Trophy> = object : Parcelable.Creator<Trophy> {
            override fun createFromParcel(`in`: Parcel): Trophy {
                return Trophy(
                    `in`
                )
            }

            override fun newArray(size: Int): Array<Trophy?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    protected constructor(`in`: Parcel) : this() {
        storyId = `in`.readInt()
        id = `in`.readInt()
        title = `in`.readString() ?: ""
        shortDescription = `in`.readString() ?: ""
        description = `in`.readString() ?: ""
        isUnlocked = (`in`.readByte() == 1.toByte())
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(storyId)
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeString(shortDescription)
        dest.writeString(description)
        dest.writeByte((if (isUnlocked) 1 else 0).toByte())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Trophy

        return other.id == id && other.storyId == storyId
    }

    override fun toString(): String {
        return "Trophy(storyId : $storyId; id: $id; title:$title; shortDescription:$shortDescription; description : $description; isUnlocked=$isUnlocked)"
    }
}