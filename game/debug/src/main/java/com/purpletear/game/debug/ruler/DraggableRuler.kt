package com.purpletear.game.debug.ruler

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A draggable horizontal ruler line.
 *
 * @param ruler The ruler state to render
 * @param containerHeight Height of the container in pixels (for drag calculations)
 * @param onMove Callback when ruler is dragged, receives new position in percentage
 * @param onDelete Callback when ruler is long-pressed
 * @param modifier Additional modifier
 */
@Composable
internal fun HorizontalRulerLine(
    ruler: Ruler,
    containerHeight: Float,
    onMove: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }

    // Use rememberUpdatedState to always have the latest callbacks
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentPosition by rememberUpdatedState(ruler.position)

    val yOffset = with(density) {
        (ruler.position * containerHeight).roundToInt()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .offset { IntOffset(0, yOffset) }
            .background(if (isDragging) Color.Yellow else ruler.color)
            .pointerInput(ruler.id) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPos = currentPosition + dragAmount.y / containerHeight
                        currentOnMove(newPos.coerceIn(0f, 1f))
                    }
                )
            }
            .pointerInput(ruler.id) {
                detectTapGestures(
                    onLongPress = { currentOnDelete() }
                )
            }
    )
}

/**
 * A draggable vertical ruler line.
 *
 * @param ruler The ruler state to render
 * @param containerWidth Width of the container in pixels (for drag calculations)
 * @param onMove Callback when ruler is dragged, receives new position in percentage
 * @param onDelete Callback when ruler is long-pressed
 * @param modifier Additional modifier
 */
@Composable
internal fun VerticalRulerLine(
    ruler: Ruler,
    containerWidth: Float,
    onMove: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }

    // Use rememberUpdatedState to always have the latest callbacks
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentPosition by rememberUpdatedState(ruler.position)

    val xOffset = with(density) {
        (ruler.position * containerWidth).roundToInt()
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
            .offset { IntOffset(xOffset, 0) }
            .background(if (isDragging) Color.Yellow else ruler.color)
            .pointerInput(ruler.id) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPos = currentPosition + dragAmount.x / containerWidth
                        currentOnMove(newPos.coerceIn(0f, 1f))
                    }
                )
            }
            .pointerInput(ruler.id) {
                detectTapGestures(
                    onLongPress = { currentOnDelete() }
                )
            }
    )
}

/**
 * Renders a ruler based on its orientation.
 */
@Composable
internal fun DraggableRuler(
    ruler: Ruler,
    containerWidth: Float,
    containerHeight: Float,
    onMove: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (ruler.orientation) {
        RulerOrientation.HORIZONTAL -> {
            HorizontalRulerLine(
                ruler = ruler,
                containerHeight = containerHeight,
                onMove = onMove,
                onDelete = onDelete,
                modifier = modifier
            )
        }

        RulerOrientation.VERTICAL -> {
            VerticalRulerLine(
                ruler = ruler,
                containerWidth = containerWidth,
                onMove = onMove,
                onDelete = onDelete,
                modifier = modifier
            )
        }
    }
}
