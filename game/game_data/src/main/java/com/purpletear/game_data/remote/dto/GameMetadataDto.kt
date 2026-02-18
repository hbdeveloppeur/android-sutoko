package com.purpletear.game_data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.GameMetadata

@Keep
data class GameMetadataDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("catchingPhrase") val catchingPhrase: String,
    @SerializedName("categories") val categories: List<String>,
)

fun GameMetadataDto.toDomain(): GameMetadata = GameMetadata(
    title = title,
    description = description,
    catchingPhrase = catchingPhrase,
    categories = categories
)