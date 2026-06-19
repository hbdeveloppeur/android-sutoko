package com.purpletear.game.data.local.dto

import androidx.annotation.Keep

@Keep
data class ChapterMetadataDto(
    val title: String,
    val description: String = ""
)
