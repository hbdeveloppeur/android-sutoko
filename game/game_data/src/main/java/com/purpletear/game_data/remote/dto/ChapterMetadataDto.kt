package com.purpletear.game_data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChapterMetadataDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
)
