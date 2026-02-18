package com.purpletear.core.presentation.components.icon

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

sealed class Icon(open val offsetX: Int = 0, open val offsetY: Int = 0) {
    data class Image(
        @DrawableRes val drawableId: Int,
        override val offsetX: Int = 0,
        override val offsetY: Int = 0,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
    ) : Icon(offsetX, offsetY)

    data class LottieAnimation(
        @RawRes val drawableId: Int,
        override val offsetX: Int = 0,
        override val offsetY: Int = 0,
        val iteration: Int = 1,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
    ) : Icon(offsetX, offsetY)
}

@Composable
fun IconComposable(
    icon: Icon,
    modifier: Modifier = Modifier,
) {
    when (icon) {
        is Icon.Image -> Image(
            painter = painterResource(id = icon.drawableId),
            contentDescription = null,
            modifier = modifier.then(Modifier.graphicsLayer {
                translationX = icon.offsetX.toFloat()
                translationY = icon.offsetY.toFloat()
                scaleX = icon.scaleX
                scaleY = icon.scaleY
            })
        )

        is Icon.LottieAnimation -> {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(icon.drawableId))
            Box(
                modifier = modifier.then(Modifier.graphicsLayer {
                    translationX = icon.offsetX.toFloat()
                    translationY = icon.offsetY.toFloat()
                    scaleX = icon.scaleX
                    scaleY = icon.scaleY
                })
            ) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = true,
                    iterations = icon.iteration
                )
            }
        }
    }
}
