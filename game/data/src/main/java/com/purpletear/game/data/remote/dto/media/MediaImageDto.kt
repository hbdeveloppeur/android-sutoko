package com.purpletear.game.data.remote.dto.media

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.core.domain.model.MediaImage

/**
 * Data Transfer Object for MediaImage.
 */
@Keep
class MediaImageDto(
    id: Long,
    type: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("bytes") val bytes: Int,
    @SerializedName("directory") val directory: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("filename") val filename: String,
) : MediaDto(id, type)

/**
 * Extension function to convert MediaImageDto to MediaImage domain model.
 */
fun MediaImageDto.toDomain(): MediaImage {
    return MediaImage(
        id = id,
        type = type,
        width = width,
        height = height,
        bytes = bytes,
        directory = directory,
        mimeType = mimeType,
        filename = filename,
    )
}