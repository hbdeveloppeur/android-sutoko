package com.purpletear.sutoko.version.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST version/by-name
 */
data class VersionByNameRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("langCode") val languageCode: String,
)