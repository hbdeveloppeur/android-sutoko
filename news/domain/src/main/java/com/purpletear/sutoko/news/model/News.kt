package com.purpletear.sutoko.news.model

import androidx.annotation.Keep
import com.purpletear.sutoko.core.domain.appaction.AppAction
import com.purpletear.sutoko.core.domain.model.MediaImage

/**
 * Domain model for News.
 */
@Keep
data class News(
    val id: Long,
    val link: String?,
    val os: String,
    val createdAt: Long,
    val publishDate: Long,
    val releaseDateAndroid: Long?,
    val media: MediaImage,
    val untilDate: Long,
    val untilVersionExclusive: Int?,
    val metadata: NewsMetadata,
    val action: AppAction?,
)
