package com.purpletear.sutoko.game.model.scene

import androidx.annotation.Keep

/**
 * A scene defines a visual background configuration for the game.
 * Can be an image, video, or solid color with optional overlay effects.
 */
@Keep
data class Scene(
    val id: Int,
    val name: String,
    val configuration: SceneConfiguration
)

/**
 * Configuration details for rendering a scene.
 */
@Keep
data class SceneConfiguration(
    val backgroundType: BackgroundType,
    val asset: SceneAsset?,
    val filterOpacity: Int = 0,
    val filterColorCode: String? = null,
    val imagePositionX: Float? = null
)

/**
 * Asset reference for image or video content.
 */
@Keep
data class SceneAsset(
    val id: Int,
    val storagePath: String,
    val thumbnailPath: String?,
    val width: Int,
    val height: Int,
    val mimeType: String
)

/**
 * Type of background media for a scene.
 */
@Keep
enum class BackgroundType {
    IMAGE,
    VIDEO,
    COLOR,
}
