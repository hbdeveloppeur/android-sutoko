package com.purpletear.smsgame.activities.smsgame.objects

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Contains the Story's storyMetadata
 * @see purpletear.fr.histoiresdhorreur.story.Story
 */
class StoryMetadata : Parcelable {

    // Story's name
    @SerializedName("story_id")
    var storyId: String = "-1"
        private set

    // Starting conversation's name
    @SerializedName("title")
    var storyTitle: String = ""
        private set

    // Starting conversation's name
    @SerializedName("description")
    var storyDescription: String = ""
        private set

    // Story's id
    @SerializedName("author")
    var authorName: String = ""
        private set

    // Minimal version required to play the story
    @SerializedName("archive_size")
    var archiveSize: Float = 1f
        private set

    // Minimal version required to play the story
    @SerializedName("min_app_version_required")
    var minAppVersionRequired: String = "1.0.0"
        private set


    init {

    }

    constructor()

    // Parcelable methods
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(`in`: Parcel) {
        this.storyId = `in`.readString() ?: "-1"
        this.storyTitle = `in`.readString() ?: ""
        this.minAppVersionRequired = `in`.readString() ?: ""
        this.storyDescription = `in`.readString() ?: ""
        this.authorName = `in`.readString() ?: ""
        this.archiveSize = `in`.readFloat()
        this.minAppVersionRequired = `in`.readString() ?: ""
    }

    constructor(
        storyId: String,
        storyTitle: String,
        storyDescription: String,
        authorName: String,
        archiveSize: Float,
        minAppVersionRequired: String
    ) {
        this.storyId = storyId
        this.storyTitle = storyTitle
        this.storyDescription = storyDescription
        this.authorName = authorName
        this.archiveSize = archiveSize
        this.minAppVersionRequired = minAppVersionRequired
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.storyId)
        dest.writeString(this.storyTitle)
        dest.writeString(this.minAppVersionRequired)
        dest.writeString(this.storyDescription)
        dest.writeString(this.authorName)
        dest.writeFloat(this.archiveSize)
        dest.writeString(this.minAppVersionRequired)
    }

    override fun toString(): String {
        return "{storyId : $storyId, storyTitle : $storyTitle, " +
                "minAppVersionRequired : $minAppVersionRequired, storyDescription : $storyDescription, " +
                "authorName : $authorName, archiveSize : $archiveSize, minAppVersionRequired : $minAppVersionRequired}"
    }


    companion object {

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<StoryMetadata> =
            object : Parcelable.Creator<StoryMetadata> {
                override fun createFromParcel(`in`: Parcel): StoryMetadata {
                    return StoryMetadata(`in`)
                }

                override fun newArray(size: Int): Array<StoryMetadata?> {
                    return arrayOfNulls(size)
                }
            }
    }

}