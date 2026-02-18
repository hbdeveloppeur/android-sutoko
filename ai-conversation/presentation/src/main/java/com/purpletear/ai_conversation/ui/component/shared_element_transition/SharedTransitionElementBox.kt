package com.purpletear.ai_conversation.ui.component.shared_element_transition

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.ui.common.utils.SharedElementTransitionState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
internal fun SharedElementTransitionBox(
    transitionState: SharedElementTransitionState,
    onStopExpend: () -> Unit = {}
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp.coerceAtMost(500.dp)
    val screenHeight = configuration.screenHeightDp.dp.coerceAtMost(500.dp)


    val animatableOffsetX = remember { Animatable(transitionState.imagePosition.x) }
    val animatableOffsetY = remember { Animatable(transitionState.imagePosition.y) }
    val animatableRadius = remember { Animatable(50f) }
    val animatableColorAlpha = remember { Animatable(if (transitionState.expanded) 1f else 0f) }

    val animatableWidthFraction =
        remember { Animatable(if (transitionState.expanded) 1f else 0f) }
    val animatableHeightFraction =
        remember { Animatable(if (transitionState.expanded) 1f else 0f) }
    val imageAlpha = remember { Animatable(if (transitionState.expanded) 1f else 0f) }
    val coroutineScope = rememberCoroutineScope()
    val zoomable = rememberZoomState()


    LaunchedEffect(transitionState.expanded) {
        if (transitionState.expanded) {
            val endOffsetX = 0f
            val endOffsetY = (screenHeight.value)
            coroutineScope {

                launch { animatableOffsetX.snapTo(transitionState.imagePosition.x) }
                launch { animatableOffsetY.snapTo(transitionState.imagePosition.y) }

                launch {
                    animatableColorAlpha.animateTo(
                        1f,
                        tween(500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableOffsetX.animateTo(
                        endOffsetX,
                        tween(500, easing = FastOutSlowInEasing)
                    )
                }
                launch { animatableRadius.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
                launch {
                    animatableOffsetY.animateTo(
                        endOffsetY,
                        tween(500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableWidthFraction.animateTo(
                        1f,
                        tween(500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableHeightFraction.animateTo(
                        1f,
                        tween(500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    imageAlpha.animateTo(
                        1f,
                        tween(500, easing = FastOutSlowInEasing)
                    )
                }
            }
        } else {
            coroutineScope {
                launch {
                    animatableWidthFraction.animateTo(
                        transitionState.imageSize.width / screenWidth.value,
                        tween(200, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableHeightFraction.animateTo(
                        transitionState.imageSize.width / screenWidth.value,
                        tween(200, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableColorAlpha.animateTo(
                        0f,
                        tween(200, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableOffsetX.animateTo(
                        transitionState.imagePosition.x,
                        tween(200, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatableOffsetY.animateTo(
                        transitionState.imagePosition.y,
                        tween(200, easing = FastOutSlowInEasing)
                    )
                }
                launch { animatableRadius.animateTo(50f, tween(200, easing = FastOutSlowInEasing)) }
                launch { imageAlpha.animateTo(0f, tween(200, easing = FastOutSlowInEasing)) }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(animatableColorAlpha.value))
            .then(
                if (transitionState.expanded) {
                    Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() } // This is mandatory
                        ) {
                            onStopExpend()
                        }
                        .zoomable(zoomable)
                } else {
                    Modifier
                }
            )
    ) {

        Box(
            Modifier

                .size(screenWidth, screenWidth)
                .graphicsLayer {
                    scaleX = animatableWidthFraction.value
                    scaleY = animatableHeightFraction.value
                    translationX = animatableOffsetX.value
                    translationY = animatableOffsetY.value
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .then(
                    if (transitionState.expanded) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {

                                },
                                onDrag = { _, dragAmount ->
                                    coroutineScope.launch {
                                        animatableOffsetX.snapTo(animatableOffsetX.value + dragAmount.x)
                                    }
                                    coroutineScope.launch {
                                        animatableOffsetY.snapTo(animatableOffsetY.value + dragAmount.y)
                                    }
                                },
                                onDragEnd = {
                                    onStopExpend()
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                )
                .alpha(imageAlpha.value)

        ) {

            AsyncImage(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            percent = animatableRadius.value.toInt()
                        )
                    )
                    .offset { IntOffset.Zero }
                    .size(screenWidth, screenWidth),

                model = ImageRequest.Builder(LocalContext.current)
                    .data(transitionState.url)
                    .crossfade(true)
                    .build(),

                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
    }
}
