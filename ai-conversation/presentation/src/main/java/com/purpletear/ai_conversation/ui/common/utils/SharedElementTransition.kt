package com.purpletear.ai_conversation.ui.common.utils

import androidx.annotation.Keep
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

@Keep
@Stable
internal data class SharedElementTransitionState(
    val url: String = "",
    val imageSize: Size = Size.Zero,
    val imagePosition: Offset = Offset.Zero,
    val expanded: Boolean = false
)

@Composable
internal fun SharedElementTransition(
    modifier: Modifier = Modifier,
    url: String,
    element: MutableState<SharedElementTransitionState>,
    content: @Composable BoxScope.() -> Unit
) {
    val imagePosition = remember { mutableStateOf(Offset.Zero) }
    val imageDpSize = remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable {
                element.value = SharedElementTransitionState(
                    url = url,
                    imageSize = imageDpSize.value,
                    imagePosition = imagePosition.value,
                    expanded = !element.value.expanded
                )
            }
            .onGloballyPositioned { layoutCoordinates ->
                val size = layoutCoordinates.size.toSize()
                val sizeX = with(density) { size.width.toDp() }
                val sizeY = with(density) { size.height.toDp() }
                imageDpSize.value = Size(sizeX.value, sizeY.value)
                val pos = layoutCoordinates
                    .positionInWindow()

                imagePosition.value = Offset(pos.x.toFloat(), pos.y.toFloat())

            },
        content = {
            content()
        }
    )
}