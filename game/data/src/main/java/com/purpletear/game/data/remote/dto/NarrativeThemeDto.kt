package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.game.NarrativeTheme

/**
 * Data Transfer Object for a narrative theme.
 */
@Keep
data class NarrativeThemeDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
)

fun NarrativeThemeDto.toDomain(): NarrativeTheme = NarrativeTheme(
    id = id,
    name = name,
)
