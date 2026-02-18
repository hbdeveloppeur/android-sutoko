package com.purpletear.news_data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.news.model.NewsMetadata

@Keep
data class NewsMetadataDto(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("catchingPhrase") val catchingPhrase: String,
)

fun NewsMetadataDto.toDomain(): NewsMetadata {
    return NewsMetadata(
        title = title,
        subtitle = subtitle,
        catchingPhrase = catchingPhrase,
    )
}