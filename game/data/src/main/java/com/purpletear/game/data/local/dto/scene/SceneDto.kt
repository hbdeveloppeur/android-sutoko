package com.purpletear.game.data.local.dto.scene

import com.google.gson.annotations.SerializedName

/**
 * DTO for scenes.json parsing.
 */
data class SceneDto(
    val id: Int,
    val name: String,
    val configuration: SceneConfigurationDto
)

data class SceneConfigurationDto(
    val id: Int,
    val asset: SceneAssetDto?,
    @SerializedName("backgroundType")
    val backgroundType: String,
    val filterOpacity: Int?,
    val filterColorCode: String?,
    val imagePositionX: Float?
)

data class SceneAssetDto(
    @SerializedName("originalFilename")
    val originalFilename: String,
    val width: Int,
    val height: Int,
    val id: Int,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("fileSizeBytes")
    val fileSizeBytes: Int,
    @SerializedName("mimeType")
    val mimeType: String,
    @SerializedName("storagePath")
    val storagePath: String,
    @SerializedName("thumbnailStoragePath")
    val thumbnailStoragePath: String?
)
