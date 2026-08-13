package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.Asset

@Keep
data class AssetDto(
    @SerializedName("id") val id: Long,
    // Server may omit these (seen in the wild); Gson bypasses Kotlin null-safety,
    // so the DTO must declare them nullable and fall back at the boundary.
    @SerializedName("originalFilename") val originalFilename: String? = null,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("fileSizeBytes") val fileSizeBytes: Int,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("storagePath") val storagePath: String,
    @SerializedName("thumbnailStoragePath") val thumbnailStoragePath: String? = null,
)

fun AssetDto.toDomain(): Asset {
    return Asset(
        id = id,
        originalFilename = originalFilename.orEmpty(),
        width = width,
        height = height,
        createdAt = createdAt,
        fileSizeBytes = fileSizeBytes,
        mimeType = mimeType,
        storagePath = storagePath,
        thumbnailStoragePath = thumbnailStoragePath.orEmpty()
    )
}
