package com.purpletear.smsgame.activities.smsgame.objects

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Keep
class Story(
    @PropertyName("sid")
    @get:PropertyName("sid")
    var id: Int = -1,

    @PropertyName("fsid")
    @get:PropertyName("fsid")
    var firebaseId: String = "",

    @PropertyName("t")
    @get:PropertyName("t")
    var title: String = "",

    @PropertyName("hpp")
    @get:PropertyName("hpp")
    var hasProfilPicture: Boolean = false,


    @PropertyName("ti")
    @get:PropertyName("ti")
    var theme: Int = 1,

    @PropertyName("v")
    @get:PropertyName("v")
    var createdWithVersion: Int = -1,

    @PropertyName("uid")
    @get:PropertyName("uid")
    var userId: String = "",

    @PropertyName("p")
    @get:PropertyName("p")
    var points: Int = -1,

    @PropertyName("hbc")
    @get:PropertyName("hbc")
    var hasBeenChecked: Boolean = false,

    @PropertyName("b")
    @get:PropertyName("b")
    var isBanned: Boolean = false,

    @PropertyName("cd")
    @get:PropertyName("cd")
    var creationDate: Timestamp = Timestamp.now(),

    @PropertyName("ud")
    @get:PropertyName("ud")
    var updateDate: Timestamp = Timestamp.now(),

    @PropertyName("sl")
    @get:PropertyName("sl")
    var suspiciousLevel: Int = 0,

    @PropertyName("io")
    @get:PropertyName("io")
    var isOnline: Boolean = false,

    @PropertyName("acn")
    @get:PropertyName("acn")
    var authorCachedName: String = "",

    @PropertyName("ci32")
    @get:PropertyName("ci32")
    var creatorImageUrl32: String = "",

    @PropertyName("ci64")
    @get:PropertyName("ci64")
    var creatorImageUrl64: String = "",

    @PropertyName("ci500")
    @get:PropertyName("ci500")
    var creatorImageUrl500: String = "",

    @PropertyName("is")
    @get:PropertyName("is")
    var isSelected: Boolean = false,

    @PropertyName("likes")
    @get:PropertyName("likes")
    var likes: Int = 0,

    @PropertyName("icg")
    @get:PropertyName("icg")
    var isChoiceGame: Boolean = false
) : Parcelable {


    enum class AvailableThemes(val id: Int) {
        HORROR(1),
        DRAMA(2),
        LOVE(3),
    }


    constructor(title: String, themeId: Int) : this() {
        this.title = title
        this.theme = themeId
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Story

        if (!isOnline && !other.isOnline) {
            return id == other.id
        }

        return id == other.id && firebaseId == other.firebaseId && userId == other.userId
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + theme
        return result
    }

    companion object {

        /**
         * Returns a Cards array from a QuerySnapshot
         * @param stories : QuerySnapshot
         * @return ArrayList<Story>
         */
        fun getStoriesFromFirebaseDocumentSnapshot(stories: QuerySnapshot): List<Story> {
            val array = ArrayList<Story>()
            for (snapshot in stories) {
                array.add(snapshot.toObject(Story::class.java))
            }
            return array
        }

        fun getStoryChaptersFromFirebaseDocumentSnapshot(stories: QuerySnapshot): List<StoryChapter> {
            val array = ArrayList<StoryChapter>()
            for (snapshot in stories) {
                array.add(snapshot.toObject(StoryChapter::class.java))
            }
            return array
        }
    }
}