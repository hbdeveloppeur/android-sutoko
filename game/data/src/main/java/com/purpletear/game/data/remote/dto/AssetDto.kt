package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.Asset

@Keep
data class AssetDto(
    @SerializedName("id") val id: Long,
    @SerializedName("originalFilename") val originalFilename: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("fileSizeBytes") val fileSizeBytes: Int,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("storagePath") val storagePath: String,
    @SerializedName("thumbnailStoragePath") val thumbnailStoragePath: String,
)

fun AssetDto.toDomain(): Asset {
    return Asset(
        id = id,
        originalFilename = originalFilename,
        width = width,
        height = height,
        createdAt = createdAt,
        fileSizeBytes = fileSizeBytes,
        mimeType = mimeType,
        storagePath = storagePath,
        thumbnailStoragePath = thumbnailStoragePath
    )
}
