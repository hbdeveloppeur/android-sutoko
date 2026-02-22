package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.GameMetadata

@Keep
data class GameMetadataDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("lang") val lang: String?,
)

fun GameMetadataDto.toDomain(): GameMetadata = GameMetadata(
    title = title,
    description = description,
    lang = lang
)
