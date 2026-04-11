package com.purpletear.game.data.mapper

import com.purpletear.game.data.local.dto.scene.SceneAssetDto
import com.purpletear.game.data.local.dto.scene.SceneConfigurationDto
import com.purpletear.game.data.local.dto.scene.SceneDto
import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.scene.BackgroundType
import com.purpletear.sutoko.game.model.scene.Scene
import com.purpletear.sutoko.game.model.scene.SceneConfiguration
import com.purpletear.sutoko.game.provider.GamePathProvider
import java.io.File

/**
 * Maps Scene DTOs to domain models.
 */
object SceneMapper {

    fun SceneDto.toDomain(gameId: String, pathProvider: GamePathProvider): Scene = Scene(
        id = id,
        name = name,
        configuration = configuration.toDomain(gameId, pathProvider)
    )

    private fun SceneConfigurationDto.toDomain(gameId: String, pathProvider: GamePathProvider): SceneConfiguration {
        val domainAsset = asset?.toDomain()
        return SceneConfiguration(
            backgroundType = parseBackgroundType(backgroundType),
            asset = domainAsset,
            filterOpacity = filterOpacity ?: 0,
            filterColorCode = filterColorCode,
            imagePositionX = imagePositionX,
            resolvedPath = domainAsset?.resolveLocalPath(gameId, pathProvider)
        )
    }

    private fun SceneAssetDto.toDomain(): Asset = Asset(
        id = id.toLong(),
        originalFilename = originalFilename,
        width = width,
        height = height,
        createdAt = createdAt,
        fileSizeBytes = fileSizeBytes,
        mimeType = mimeType,
        storagePath = storagePath,
        thumbnailStoragePath = thumbnailStoragePath ?: ""
    )

    private fun Asset.resolveLocalPath(gameId: String, pathProvider: GamePathProvider): String? {
        if (storagePath.isBlank()) return null
        val fileName = storagePath.substringAfterLast("/")
        val basePath = pathProvider.getStoryDirectoryPath(gameId)
        return "$basePath${File.separator}assets${File.separator}$fileName"
    }

    private fun parseBackgroundType(type: String): BackgroundType = when (type.lowercase()) {
        "image" -> BackgroundType.IMAGE
        "video" -> BackgroundType.VIDEO
        "color" -> BackgroundType.COLOR
        else -> BackgroundType.IMAGE
    }
}
