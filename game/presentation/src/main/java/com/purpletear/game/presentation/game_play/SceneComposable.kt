package com.purpletear.game.presentation.game_play

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.game.presentation.game_play.components.background.VideoBackground
import com.purpletear.sutoko.game.model.scene.BackgroundType
import com.purpletear.sutoko.game.model.scene.Scene
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

/**
 * Displays a scene with crossfade animation when the scene changes.
 * Supports video, image, and solid color backgrounds with optional filter overlay.
 *
 * @param scene The scene to display, or null for a black background
 * @param gameId The game/story ID used to resolve local file paths
 * @param modifier The modifier to be applied to the component
 */
@Composable
internal fun SceneComposable(
    scene: Scene?,
    gameId: String?,
    modifier: Modifier = Modifier,
    viewModel: SceneComposableViewModel = hiltViewModel()
) {
    AnimatedContent(
        targetState = scene,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = 500)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 500))
        },
        label = "SceneCrossfade"
    ) { targetScene ->
        SceneContent(
            scene = targetScene,
            gameId = gameId,
            getAssetPath = { id, path -> viewModel.resolveAssetPath(id, path) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Renders the actual scene content (video, image, or color) with optional filter.
 */
@Composable
private fun SceneContent(
    scene: Scene?,
    gameId: String?,
    getAssetPath: (String, String) -> String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Background layer
        when (val config = scene?.configuration) {
            null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )

            else -> when (config.backgroundType) {
                BackgroundType.VIDEO -> {
                    config.asset?.let { asset ->
                        val fullPath = gameId?.let { getAssetPath(it, asset.storagePath) }
                        if (fullPath != null) {
                            VideoBackground(
                                videoPath = fullPath,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                            )
                        }
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }

                BackgroundType.IMAGE -> {
                    config.asset?.let { asset ->
                        val fullPath = gameId?.let { getAssetPath(it, asset.storagePath) }
                        if (fullPath != null) {
                            ImageBackground(
                                imagePath = fullPath,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                            )
                        }
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }

                BackgroundType.COLOR -> {
                    val color = config.filterColorCode?.let { parseColor(it) } ?: Color.Black
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                    )
                }
            }
        }

        // Filter overlay layer (applies to all background types except pure color)
        val filterOpacity = scene?.configuration?.filterOpacity ?: 0
        val filterColorCode = scene?.configuration?.filterColorCode

        if (filterOpacity > 0 && filterColorCode != null && scene?.configuration?.backgroundType != BackgroundType.COLOR) {
            val filterColor = parseColor(filterColorCode).copy(alpha = filterOpacity / 100f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filterColor)
            )
        }
    }
}

/**
 * Displays an image background using Coil's AsyncImage.
 * Image scales to fill the container with ContentScale.Crop.
 */
@Composable
private fun ImageBackground(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(File(imagePath))
            .crossfade(false)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

/**
 * Parses a hex color string into a Compose Color.
 * Supports formats: #RRGGBB, #AARRGGBB, RRGGBB, ARRGGBB
 */
private fun parseColor(colorCode: String): Color {
    val trimmed = colorCode.trim()

    val hex = when {
        trimmed.startsWith("#") -> trimmed.substring(1)
        else -> trimmed
    }

    return try {
        val colorInt = when (hex.length) {
            6 -> android.graphics.Color.parseColor("#$hex")
            8 -> {
                val alpha = hex.substring(0, 2).toInt(16)
                val rgb = hex.substring(2).toInt(16)
                (alpha shl 24) or (rgb and 0x00FFFFFF)
            }

            else -> return Color.Black
        }
        Color(colorInt)
    } catch (_: Exception) {
        Color.Black
    }
}

/**
 * ViewModel for SceneComposable that provides path resolution.
 */
@HiltViewModel
class SceneComposableViewModel @Inject constructor(
    private val gamePathProvider: GamePathProvider
) : ViewModel() {

    /**
     * Resolves a storage path to a full local file path.
     *
     * @param gameId The game/story ID
     * @param storagePath The relative storage path from SceneAsset
     * @return The full absolute path to the file, or null if gameId is null
     */
    fun resolveAssetPath(gameId: String, storagePath: String): String {
        val fileName = storagePath.substringAfterLast("/")
        val basePath = gamePathProvider.getStoryDirectoryPath(gameId)
        return "$basePath${File.separator}assets${File.separator}$fileName"
    }
}
