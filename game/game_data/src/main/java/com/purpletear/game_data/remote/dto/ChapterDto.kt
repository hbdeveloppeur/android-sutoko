package com.purpletear.game_data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.Chapter

/**
 * Data Transfer Object for Chapter.
 */
@Keep
data class ChapterDto(
    @SerializedName("id") val id: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("alternative") val alternative: String,
    @SerializedName("releaseDateAndroid") val releaseDate: Long,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("minAppCodeAndroid") val minAppCode: Int,
    @SerializedName("switchable") val switchable: Boolean,
    @SerializedName("minStoryVersion") val minStoryVersion: String,
    @SerializedName("metadata") val metadata: ChapterMetadataDto,
    @SerializedName("isAvailableAndroid") val isAvailable: Boolean = false,

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
        minAppCode = minAppCode,
        switchable = switchable,
        minStoryVersion = minStoryVersion,
        title = metadata.title,
        description = metadata.description,
        isAvailable = isAvailable,
        releaseDate = releaseDate,

        )
}

/**
 * Extension function to convert a list of ChapterDto to a list of Chapter domain models.
 */
fun List<ChapterDto>.toDomain(): List<Chapter> {
    return map { it.toDomain() }
}
