package com.purpletear.game.presentation.game_play.components.image_viewer

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.hypot

private const val ANIM_DURATION_OPEN_MS = 350
private const val ANIM_DURATION_CLOSE_MS = 300
private const val SNAP_BACK_DURATION_MS = 200
private const val DISMISS_DRAG_THRESHOLD_DP = 150f
private const val CORNER_RADIUS_EXPANDED_DP = 16f
private const val ZOOM_DISMISS_TOLERANCE = 1.01f

internal enum class SwipeToDismissDirection {
    ANY,
    LEFT,
}

@Composable
internal fun ImageViewerOverlay(
    imageModel: Any?,
    sourceBounds: Rect?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    swipeToDismissDirection: SwipeToDismissDirection = SwipeToDismissDirection.ANY,
) {
    if (sourceBounds == null) return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Target preserves thumbnail aspect ratio and fills screen width
    val targetWidth = screenWidthPx
    val targetHeight = sourceBounds.height / sourceBounds.width * targetWidth
    val targetTop = (screenHeightPx - targetHeight) / 2f

    val scope = rememberCoroutineScope()
    val zoomState = rememberZoomState()
    val isZoomedOut by remember { derivedStateOf { zoomState.scale <= ZOOM_DISMISS_TOLERANCE } }

    val alpha = remember { Animatable(0f) }
    val offsetX = remember { Animatable(sourceBounds.left) }
    val offsetY = remember { Animatable(sourceBounds.top) }
    val animScaleX = remember { Animatable(sourceBounds.width / targetWidth) }
    val animScaleY = remember { Animatable(sourceBounds.height / targetHeight) }
    val radius = remember { Animatable(CORNER_RADIUS_EXPANDED_DP) }

    LaunchedEffect(imageModel, isVisible) {
        if (isVisible) {
            zoomState.reset()
            offsetX.snapTo(sourceBounds.left)
            offsetY.snapTo(sourceBounds.top)
            animScaleX.snapTo(sourceBounds.width / targetWidth)
            animScaleY.snapTo(sourceBounds.height / targetHeight)
            radius.snapTo(CORNER_RADIUS_EXPANDED_DP)
            alpha.animateTo(1f, tween(ANIM_DURATION_OPEN_MS, easing = FastOutSlowInEasing))
            launch { offsetX.animateTo(0f, tween(ANIM_DURATION_OPEN_MS, easing = FastOutSlowInEasing)) }
            launch { offsetY.animateTo(targetTop, tween(ANIM_DURATION_OPEN_MS, easing = FastOutSlowInEasing)) }
            launch { animScaleX.animateTo(1f, tween(ANIM_DURATION_OPEN_MS, easing = FastOutSlowInEasing)) }
            launch { animScaleY.animateTo(1f, tween(ANIM_DURATION_OPEN_MS, easing = FastOutSlowInEasing)) }
            launch { radius.animateTo(0f, tween(ANIM_DURATION_OPEN_MS, easing = FastOutSlowInEasing)) }
        } else {
            launch { alpha.animateTo(0f, tween(ANIM_DURATION_CLOSE_MS, easing = FastOutSlowInEasing)) }
            launch { offsetX.animateTo(sourceBounds.left, tween(ANIM_DURATION_CLOSE_MS, easing = FastOutSlowInEasing)) }
            launch { offsetY.animateTo(sourceBounds.top, tween(ANIM_DURATION_CLOSE_MS, easing = FastOutSlowInEasing)) }
            launch { animScaleX.animateTo(sourceBounds.width / targetWidth, tween(ANIM_DURATION_CLOSE_MS, easing = FastOutSlowInEasing)) }
            launch { animScaleY.animateTo(sourceBounds.height / targetHeight, tween(ANIM_DURATION_CLOSE_MS, easing = FastOutSlowInEasing)) }
            launch { radius.animateTo(CORNER_RADIUS_EXPANDED_DP, tween(ANIM_DURATION_CLOSE_MS, easing = FastOutSlowInEasing)) }
        }
    }

    if (alpha.value <= 0f && !isVisible) return

    BackHandler(enabled = isVisible) {
        onDismiss()
    }

    val snapBack: suspend () -> Unit = {
        offsetX.animateTo(0f, tween(SNAP_BACK_DURATION_MS))
        offsetY.animateTo(targetTop, tween(SNAP_BACK_DURATION_MS))
    }

    val shouldDismissAfterDrag: () -> Boolean = {
        when (swipeToDismissDirection) {
            SwipeToDismissDirection.ANY -> {
                val dragDist = hypot(
                    offsetX.value - 0f,
                    offsetY.value - targetTop
                )
                dragDist > DISMISS_DRAG_THRESHOLD_DP
            }
            SwipeToDismissDirection.LEFT -> {
                offsetX.value < -DISMISS_DRAG_THRESHOLD_DP
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(Color.Black)
            .then(
                if (isVisible && alpha.value > 0.5f) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
                        .zoomable(zoomState)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .size(
                    with(density) { targetWidth.toDp() },
                    with(density) { targetHeight.toDp() }
                )
                .graphicsLayer {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    scaleX = animScaleX.value
                    scaleY = animScaleY.value
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .clip(RoundedCornerShape(radius.value.dp))
                .then(
                    if (isZoomedOut) {
                        Modifier.pointerInput(swipeToDismissDirection) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (shouldDismissAfterDrag()) {
                                        onDismiss()
                                    } else {
                                        scope.launch { snapBack() }
                                    }
                                }
                            ) { _, dragAmount ->
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageModel)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
        }
    }
}
