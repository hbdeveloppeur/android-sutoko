package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.Chapter

/**
 * Data Transfer Object for Chapter.
 */
@Keep
data class ChapterDto(
    @SerializedName("id") val id: String,
    @SerializedName("number") val number: Int,
    @SerializedName("alternative") val alternative: String,
    @SerializedName("releaseDate") val releaseDate: Long,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("story") val story: String,
    @SerializedName("metas") val metas: ChapterMetasDto,
    @SerializedName("publishedVersion") val publishedVersion: Int,
    @SerializedName("code") val code: String
)

/**
 * Data Transfer Object for Chapter Metas.
 */
@Keep
data class ChapterMetasDto(
    @SerializedName("id") val id: Int,
    @SerializedName("lang") val lang: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)

/**
 * Extension function to convert ChapterDto to Chapter domain model.
 */
fun ChapterDto.toDomain(): Chapter {
    return Chapter(
        id = id,
        number = number,
        alternative = alternative,
        createdAt = createdAt,
        releaseDate = releaseDate,
        story = story,
        title = metas.title,
        description = metas.description,
        publishedVersion = publishedVersion,
        code = code
    )
}

/**
 * Extension function to convert a list of ChapterDto to a list of Chapter domain models.
 */
fun List<ChapterDto>.toDomain(): List<Chapter> {
    return map { it.toDomain() }
}
