package com.purpletear.sutoko.version.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.core.domain.model.MediaImage
import com.purpletear.sutoko.version.model.Version
import com.purpletear.sutoko.version.model.VersionMetadata

/**
 * DTO for Version returned by portal API.
 */
data class VersionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("versionNumber") val versionNumber: Int,
    @SerializedName("versionCode") val versionCode: String,
    @SerializedName("publishDateAndroid") val publishDateAndroid: Long,
    @SerializedName("backgroundImage") val backgroundImage: MediaImageDto,
    @SerializedName("metadata") val metadata: VersionMetadataDto,
)

fun VersionDto.toDomain(): Version = Version(
    id = id,
    isOnline = isOnline,
    name = name,
    versionNumber = versionNumber,
    versionCode = versionCode,
    publishDate = publishDateAndroid,
    backgroundImage = backgroundImage.toDomain(),
    metadata = metadata.toDomain()
)

/**
 * MediaImage DTO used by Version payloads.
 */
data class MediaImageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("bytes") val bytes: Int,
    @SerializedName("directory") val directory: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("filename") val filename: String,
)

fun MediaImageDto.toDomain(): MediaImage = MediaImage(
    id = id,
    type = type,
    width = width,
    height = height,
    bytes = bytes,
    directory = directory,
    mimeType = mimeType,
    filename = filename
)

/**
 * Metadata DTO for Version.
 */
data class VersionMetadataDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("language") val language: String,
)

fun VersionMetadataDto.toDomain(): VersionMetadata = VersionMetadata(
    id = id,
    title = title,
    subtitle = subtitle,
    languageCode = language
)
