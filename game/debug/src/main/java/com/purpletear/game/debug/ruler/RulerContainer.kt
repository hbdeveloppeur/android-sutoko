package com.purpletear.game.debug.ruler

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged

/**
 * Container that renders all rulers and handles gesture interactions.
 *
 * Features:
 * - Renders all rulers from [RulerState]
 * - Double-tap to add horizontal/vertical rulers at tap position
 * - Drag to move rulers
 * - Long-press on ruler to delete
 *
 * @param state The ruler state to render
 * @param modifier Additional modifier
 */
@Composable
fun RulerContainer(
    state: RulerState,
    modifier: Modifier = Modifier
) {
    // Use rememberSaveable for dimensions to survive Live Edit
    var containerWidth by rememberSaveable { mutableFloatStateOf(0f) }
    var containerHeight by rememberSaveable { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerWidth = size.width.toFloat()
                containerHeight = size.height.toFloat()
            }
            .pointerInput(state) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        // Determine orientation based on tap proximity to edges
                        // Tap near top/bottom edge -> horizontal ruler
                        // Tap near left/right edge -> vertical ruler
                        // Otherwise -> add both (or we could prompt)
                        val edgeThreshold = 50f
                        val isNearHorizontalEdge = offset.y < edgeThreshold ||
                                offset.y > containerHeight - edgeThreshold
                        val isNearVerticalEdge = offset.x < edgeThreshold ||
                                offset.x > containerWidth - edgeThreshold

                        when {
                            isNearHorizontalEdge -> {
                                val position = (offset.y / containerHeight).coerceIn(0f, 1f)
                                state.add(RulerOrientation.HORIZONTAL, position)
                            }

                            isNearVerticalEdge -> {
                                val position = (offset.x / containerWidth).coerceIn(0f, 1f)
                                state.add(RulerOrientation.VERTICAL, position)
                            }

                            else -> {
                                // Add both for convenience when tapping in middle
                                val hPosition = (offset.y / containerHeight).coerceIn(0f, 1f)
                                val vPosition = (offset.x / containerWidth).coerceIn(0f, 1f)
                                state.add(RulerOrientation.HORIZONTAL, hPosition)
                                state.add(RulerOrientation.VERTICAL, vPosition)
                            }
                        }
                    }
                )
            }
    ) {
        // Render all rulers with keys for proper recomposition during Live Edit
        state.rulers.forEach { ruler ->
            key(ruler.id) {
                DraggableRuler(
                    ruler = ruler,
                    containerWidth = containerWidth,
                    containerHeight = containerHeight,
                    onMove = { newPosition -> state.move(ruler.id, newPosition) },
                    onDelete = { state.delete(ruler.id) }
                )
            }
        }
    }
}
