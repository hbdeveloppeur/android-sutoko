package com.purpletear.smsgame.activities.smsgame.objects

import android.os.Parcelable
import android.text.format.DateFormat
import androidx.annotation.Keep
import com.example.sharedelements.OnlineAssetsManager
import com.google.firebase.firestore.PropertyName
import com.purpletear.smsgame.BuildConfig
import kotlinx.parcelize.Parcelize
import purpletear.fr.purpleteartools.TableOfSymbols
import java.util.Date
import java.util.Locale

@Parcelize
@Keep
class StoryChapter(
    @PropertyName("title")
    @get:PropertyName("title")
    val title: String,

    @PropertyName("desc")
    @get:PropertyName("desc")
    val description: String,

    @PropertyName("releaseDate")
    @get:PropertyName("releaseDate")
    val releaseDate: Date?,
    @PropertyName("chapterNumber")
    @get:PropertyName("chapterNumber")
    val chapterNumber: Int,

    @PropertyName("chapterAlternative")
    @get:PropertyName("chapterAlternative")
    val chapterAlternative: String,

    @PropertyName("visibility")
    @get:PropertyName("visibility")
    val visibility: Int,

    @PropertyName("mac")
    @get:PropertyName("mac")
    val minAppCode: Int,

    @PropertyName("msv")
    @get:PropertyName("msv")
    val minStoryVersion: String?,

    @PropertyName("isSpe")
    @get:PropertyName("isSpe")
    val isSpecific: Boolean?,

    @PropertyName("uca")
    @get:PropertyName("uca")
    val userHasAccess: Boolean?
) : Parcelable {


    val chapterCode: String
        get() {
            return "${this.chapterNumber}${this.chapterAlternative.lowercase(Locale.ENGLISH)}"
        }

    fun isAvailable(): Boolean {
        return releaseDate != null && Date().after(releaseDate) && (userHasAccess ?: true)
    }

    fun isCompatible(): Boolean {
        return this.minAppCode <= BuildConfig.VERSION_CODE
    }

    fun requiresStoryUpdate(symbols: TableOfSymbols): Boolean {
        if (minStoryVersion == null) {
            throw NullPointerException()
        }
        if (symbols.getStoryVersion(symbols.gameId) == "none") {
            return true
        }
        return OnlineAssetsManager.compareVersion(
            minStoryVersion,
            symbols.getStoryVersion(symbols.gameId)
        ) == OnlineAssetsManager.VersionComparision.GREATER
    }

    constructor() : this("", "", null, -1, "a", 1, 1, "0.0.1", false, true)


    fun getReleaseDayNumber(): String? {
        releaseDate?.let {
            return DateFormat.format("dd", it) as String
        }
        return null
    }

    fun getReleaseMonthName(): String? {
        releaseDate?.let {
            return DateFormat.format("MMM", it) as String
        }
        return null
    }

    companion object {

        fun numberFromCode(code: String): Int {
            assert(code.length > 1)
            return code.substring(0, code.length - 1).toInt()
        }

        fun alternativeFromCode(code: String): String {
            assert(code.length > 1)
            return code.substring(code.length - 1)
        }

        /**
         * Creates a StoryChapter from a Chapter domain model.
         *
         * @param chapter The Chapter domain model to convert.
         * @return A new StoryChapter instance with properties mapped from the Chapter.
         */
        fun fromChapter(chapter: com.purpletear.sutoko.game.model.Chapter): StoryChapter {
            return StoryChapter(
                title = chapter.title,
                description = chapter.description,
                releaseDate = if (chapter.releaseDate > 0) Date(chapter.releaseDate) else null,
                chapterNumber = chapter.number,
                chapterAlternative = chapter.alternative,
                visibility = 1, // Default visibility
                minAppCode = chapter.minAppCode,
                minStoryVersion = chapter.minStoryVersion,
                isSpecific = false, // Default value
                userHasAccess = chapter.isAvailable
            )
        }
    }

}
