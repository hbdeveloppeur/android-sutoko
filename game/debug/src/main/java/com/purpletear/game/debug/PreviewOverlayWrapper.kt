package com.purpletear.game.debug

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.purpletear.game.debug.ruler.RulerContainer
import com.purpletear.game.debug.ruler.RulerControls
import com.purpletear.game.debug.ruler.rememberRulerState

@Preview(name = "OverlayWrapper")
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .height(52.dp)
            .aspectRatio(564f / 166f),
        drawable = R.drawable.preview_messagedest,
    ) {
        Box(
            Modifier
                .height(32.dp)
                .width(100.dp)
                .background(Color.Red)
        )
    }
}

@Composable
fun PreviewOverlayWrapper(
    imageModifier: Modifier = Modifier,
    @DrawableRes drawable: Int,
    content: @Composable () -> Unit,

    ) {
    val rulerState = rememberRulerState()

    Grid {
        Column {
            AsyncImage(
                modifier = imageModifier,
                model = drawable,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            content()
        }

        // Ruler overlay with controls
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            RulerContainer(
                state = rulerState,
                modifier = Modifier.fillMaxSize()
            )

            RulerControls(
                state = rulerState,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun Grid(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column {
            Row {
                Diagonal()
                StaticHorizontalRule()
            }
            Row(Modifier.fillMaxSize()) {
                StaticVerticalRule()
                Box {
                    content()
                }
            }
        }
    }
}

@Composable
private fun StaticVerticalRule() {
    Box(
        Modifier
            .width(14.dp)
            .fillMaxHeight()
            .background(Color.DarkGray)
    )
}

@Composable
private fun StaticHorizontalRule() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Color.DarkGray)
    )
}

@Composable
private fun Diagonal() {
    Box(
        Modifier
            .size(14.dp)
            .background(Color.Gray)
    )
}


fun Modifier.rotateLayout(degrees: Int): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val angle = ((degrees % 360) + 360) % 360
        require(angle == 0 || angle == 90 || angle == 180 || angle == 270) {
            "rotateLayout only supports 0, 90, 180, 270"
        }

        val placeable = measurable.measure(constraints)

        val w = placeable.width
        val h = placeable.height

        val rotatedWidth = if (angle == 90 || angle == 270) h else w
        val rotatedHeight = if (angle == 90 || angle == 270) w else h

        layout(rotatedWidth, rotatedHeight) {
            when (angle) {
                0 -> placeable.placeRelativeWithLayer(0, 0)

                90 -> placeable.placeRelativeWithLayer(h, 0) {
                    rotationZ = 90f
                    transformOrigin = TransformOrigin(0f, 0f)
                }

                180 -> placeable.placeRelativeWithLayer(w, h) {
                    rotationZ = 180f
                    transformOrigin = TransformOrigin(0f, 0f)
                }

                270 -> placeable.placeRelativeWithLayer(0, w) {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
        }
    }
)