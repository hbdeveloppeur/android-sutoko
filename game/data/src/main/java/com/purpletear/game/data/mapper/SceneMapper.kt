package com.purpletear.game.data.mapper

import com.purpletear.game.data.local.dto.scene.SceneAssetDto
import com.purpletear.game.data.local.dto.scene.SceneConfigurationDto
import com.purpletear.game.data.local.dto.scene.SceneDto
import com.purpletear.sutoko.game.model.scene.BackgroundType
import com.purpletear.sutoko.game.model.scene.Scene
import com.purpletear.sutoko.game.model.scene.SceneAsset
import com.purpletear.sutoko.game.model.scene.SceneConfiguration

/**
 * Maps Scene DTOs to domain models.
 */
object SceneMapper {

    fun SceneDto.toDomain(): Scene = Scene(
        id = id,
        name = name,
        configuration = configuration.toDomain()
    )

    private fun SceneConfigurationDto.toDomain(): SceneConfiguration = SceneConfiguration(
        backgroundType = parseBackgroundType(backgroundType),
        asset = asset?.toDomain(),
        filterOpacity = filterOpacity ?: 0,
        filterColorCode = filterColorCode,
        imagePositionX = imagePositionX
    )

    private fun SceneAssetDto.toDomain(): SceneAsset = SceneAsset(
        id = id,
        storagePath = storagePath,
        thumbnailPath = thumbnailStoragePath,
        width = width,
        height = height,
        mimeType = mimeType
    )

    private fun parseBackgroundType(type: String): BackgroundType = when (type.lowercase()) {
        "image" -> BackgroundType.IMAGE
        "video" -> BackgroundType.VIDEO
        "color" -> BackgroundType.COLOR
        else -> BackgroundType.IMAGE
    }
}
