package com.purpletear.news.data.remote.dto

import androidx.annotation.Keep

/**
 * Data Transfer Object for requesting news.
 *
 * @property langCode The language code for the news.
 * @property versionNumber The version code of the project.
 */
@Keep
data class NewsRequestDto(
    val langCode: String,
    val versionNumber: Int
)