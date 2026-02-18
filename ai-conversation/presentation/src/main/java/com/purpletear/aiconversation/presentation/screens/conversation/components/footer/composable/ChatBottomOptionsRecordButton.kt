package com.purpletear.aiconversation.presentation.screens.conversation.components.footer.composable

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.purpletear.aiconversation.presentation.R
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

sealed class Drag(val deleteIconAlpha: Float) {
    data object Idle : Drag(deleteIconAlpha = 0f)
    data object Pressed : Drag(deleteIconAlpha = 0.1f)
    data class Dragging(val percent: Float) :
        Drag(deleteIconAlpha = if ((percent > 10f)) 0.9f else 0.1f)

    data object Dragged : Drag(0f)
}


@Composable
fun RecordButton(
    modifier: Modifier = Modifier,
    onPress: () -> Unit = {},
    onRelease: (isCancel: Boolean) -> Unit = {},
    onDrag: (percent: Float) -> Unit = {}
) {
    val boxSize = 48.dp

    val initialOffset = remember { mutableStateOf(IntOffset.Zero) }
    val drag = remember { mutableStateOf<Drag>(Drag.Idle) }
    val offsetX = remember { Animatable(0f) }
    val size = remember { mutableStateOf(Size.Zero) }
    val coroutineScope = rememberCoroutineScope()
    val percent = remember { mutableFloatStateOf(0f) }


    LaunchedEffect(drag.value) {
        Log.d("DragValueEvolution", drag.value.toString())

        when (drag.value) {
            Drag.Idle -> Unit
            Drag.Pressed -> {
                onPress()
            }

            is Drag.Dragging -> {
                percent.floatValue = (drag.value as Drag.Dragging).percent
                onDrag(percent.floatValue)
            }

            Drag.Dragged -> {
                Log.d("AudioRecord2", "onRelease")
                onRelease(percent.floatValue > 80f)
                offsetX.animateTo(initialOffset.value.x.toFloat())
            }
        }
    }

    val density = LocalDensity.current


    LaunchedEffect(size, density) {
        val pxValue = with(density) { boxSize.toPx() }
        initialOffset.value = IntOffset(size.value.width.roundToInt() - pxValue.roundToInt(), 0)
        offsetX.snapTo(initialOffset.value.x.toFloat())
    }

    val configuration = LocalConfiguration.current

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .onGloballyPositioned { coordinates ->
                size.value = coordinates.size.toSize()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        Log.d("RecordButton", "onDragStart")
                        if (Drag.Pressed != drag.value) {
                            drag.value = Drag.Pressed
                        }
                    },
                    onDrag = { _, dragAmount ->
                        val screenWidthInPixel = configuration.screenWidthDp * density.density
                        val c = abs(offsetX.value) * 117 / screenWidthInPixel
                        val x = offsetX.value + dragAmount.x
                        drag.value = Drag.Dragging(c)

                        coroutineScope.launch {
                            offsetX.snapTo(x)
                        }
                    },
                    onDragEnd = {
                        Log.d("RecordButton", "onDragEnd")
                        if (drag.value != Drag.Dragged) {
                            drag.value = Drag.Dragged
                        }
                    },
                    onDragCancel = {
                        Log.d("RecordButton", "onDragCancel")
                        if (drag.value != Drag.Dragged) {
                            drag.value = Drag.Dragged
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (Drag.Pressed != drag.value) {
                            drag.value = Drag.Pressed
                        }
                        Log.d("RecordButton", "onPress")
                        awaitRelease()
                        Log.d("RecordButton", "onPress after awaitRelease")
                        if (drag.value == Drag.Pressed && Drag.Dragged != drag.value) {
                            drag.value = Drag.Dragged
                        }
                    },
                    onTap = {
                        Log.d("RecordButton", "onTap")
                        if (Drag.Dragged != drag.value) {
                            drag.value = Drag.Dragged
                        }
                    }
                )
            }
    ) {

        Box(
            Modifier
                .size(boxSize)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {}
                .focusable(false)


        ) {
            ChatBottomOptionsButton(
                modifier = Modifier
                    .align(Alignment.Center)
                    .pointerInput(Unit) {}
                    .focusable(false),
                isEnabled = true,
                icon = R.drawable.ic_microphone
            )
        }
    }
}

