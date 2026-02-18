package com.purpletear.sutoko.news.model

import androidx.annotation.Keep

@Keep
data class NewsMetadata(
    val title: String,
    val subtitle: String,
    val catchingPhrase: String,
)
