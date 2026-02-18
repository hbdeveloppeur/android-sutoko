package com.purpletear.news.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.news.data.remote.dto.media.MediaImageDto
import com.purpletear.news.data.remote.dto.media.toDomain
import com.purpletear.sutoko.news.model.News

/**
 * Data Transfer Object for News.
 */
@Keep
data class NewsDto(
    @SerializedName("id") val id: Long,
    @SerializedName("link") val link: String?,
    @SerializedName("os") val os: String,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("publishDate") val publishDate: Long,
    @SerializedName("releaseDateAndroid") val releaseDateAndroid: Long?,
    @SerializedName("media") val media: MediaImageDto,
    @SerializedName("untilDate") val untilDate: Long,
    @SerializedName("untilVersionExclusive") val untilVersionExclusive: Int?,
    @SerializedName("metadata") val metadata: NewsMetadataDto,
    @SerializedName("action") val action: ActionDto?,
)

/**
 * Extension function to convert NewsDto to News domain model.
 */
fun NewsDto.toDomain(): News {
    return News(
        id = id,
        link = link,
        os = os,
        createdAt = createdAt,
        publishDate = publishDate,
        releaseDateAndroid = releaseDateAndroid,
        media = media.toDomain(),
        untilDate = untilDate,
        untilVersionExclusive = untilVersionExclusive,
        metadata = metadata.toDomain(),
        action = action?.toDomain(),
    )
}

/**
 * Extension function to convert a list of NewsDto to a list of News domain models.
 */
fun List<NewsDto>.toDomain(): List<News> {
    return map { it.toDomain() }
}