package com.purpletear.game.presentation.game_play

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.purpletear.game.presentation.common.extensions.parse
import com.purpletear.game.presentation.game_play.components.background.ImageBackground
import com.purpletear.game.presentation.game_play.components.background.VideoBackground
import com.purpletear.sutoko.game.model.scene.BackgroundType
import com.purpletear.sutoko.game.model.scene.Scene
import kotlinx.coroutines.delay

private const val FILTER_FADE_DURATION: Int = 1200

@Composable
internal fun SceneComposable(
    scene: Scene?,
) {
    var displayedScene by remember { mutableStateOf<Scene?>(null) }
    var filterIsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scene) {
        if (null == scene) {
            filterIsVisible = true
            return@LaunchedEffect
        }
        filterIsVisible = true
        delay(FILTER_FADE_DURATION.toLong())
        displayedScene = scene
    }

    SceneContent(
        scene = displayedScene,
        onLoaded = {
            filterIsVisible = false
        }
    )

    MainFilter(isVisible = filterIsVisible)
}

@Composable
private fun SceneContent(
    scene: Scene?,
    onLoaded: () -> Unit
) {
    when (scene?.configuration?.backgroundType) {
        BackgroundType.VIDEO -> {
            val fullPath = scene.configuration.resolvedPath ?: return
            VideoBackground(
                videoPath = fullPath,
                onStarted = onLoaded,
                onError = { error ->
                    Log.e("VideoBackground", "Failed to load video: $fullPath", error)
                }
            )

            val filterColor = scene.configuration.filterColorCode
            val filterOpacity = scene.configuration.filterOpacity
            Filter(colorCode = filterColor, opacity = filterOpacity)
        }

        BackgroundType.IMAGE -> {
            val fullPath = scene.configuration.resolvedPath ?: return
            ImageBackground(
                imagePath = fullPath,
                onStarted = onLoaded,
                onError = { error ->
                    Log.e("ImageBackground", "Failed to load image: $fullPath", error)
                }
            )

            val filterColor = scene.configuration.filterColorCode
            val filterOpacity = scene.configuration.filterOpacity
            Filter(colorCode = filterColor, opacity = filterOpacity)
        }

        BackgroundType.COLOR -> {
            onLoaded()
            val filterColor = scene.configuration.filterColorCode
            val filterOpacity = scene.configuration.filterOpacity
            Filter(colorCode = filterColor, opacity = filterOpacity)
        }

        else -> {

        }
    }
}

@Composable
private fun Filter(colorCode: String?, opacity: Int) {
    colorCode?.let {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.parse(it).copy(alpha = (opacity * 0.01).toFloat()))
        )
    }
}


@Composable
private fun MainFilter(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(durationMillis = FILTER_FADE_DURATION)),
        exit = fadeOut(tween(durationMillis = FILTER_FADE_DURATION)),
    )
    {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}