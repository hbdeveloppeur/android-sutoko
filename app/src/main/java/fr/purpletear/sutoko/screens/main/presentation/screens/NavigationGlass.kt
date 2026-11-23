package fr.purpletear.sutoko.screens.main.presentation.screens

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal class NavigationBackdrop(val source: GraphicsLayer) {
    var origin by mutableStateOf(Offset.Unspecified)
        private set

    fun capture(): Modifier = Modifier
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawWithContent {
            drawIntoCanvas { canvas ->
                if (canvas.nativeCanvas.isHardwareAccelerated) {
                    source.record { this@drawWithContent.drawContent() }
                    drawLayer(source)
                } else {
                    drawContent()
                }
            }
        }
}

@Composable
internal fun rememberNavigationBackdrop(): NavigationBackdrop? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val source = rememberGraphicsLayer()
    return remember(source) { NavigationBackdrop(source) }
}

@Composable
internal fun Modifier.navigationGlass(backdrop: NavigationBackdrop?): Modifier {
    val sample = if (backdrop != null) rememberGraphicsLayer() else null
    var origin by remember { mutableStateOf(Offset.Unspecified) }

    return onGloballyPositioned { origin = it.positionInRoot() }
        .drawWithCache {
            if (size.width <= 0f || size.height <= 0f) {
                return@drawWithCache onDrawBehind {}
            }

            val radius = size.height / 2f
            val clip = Path().apply { addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(radius))) }
            val blurRadius = 9.dp.toPx()
            val overscan = (2f * blurRadius).roundToInt()
            val bufferSize = IntSize(
                size.width.roundToInt() + 2 * overscan,
                size.height.roundToInt() + 2 * overscan,
            )
            sample?.renderEffect = BlurEffect(blurRadius, blurRadius, TileMode.Clamp)

            val tint = Brush.verticalGradient(
                0f to Color(0x28FFFFFF),
                0.16f to Color(0x24C7C7C7),
                0.54f to Color(0x26151515),
                1f to Color(0x58080808),
            )
            val gradientDirection = Offset(0.36f / size.width, 0.64f / size.height)
            val gradientEnd = gradientDirection /
                (gradientDirection.x * gradientDirection.x + gradientDirection.y * gradientDirection.y)
            val edge = Brush.linearGradient(
                0f to Color(0x4DFFFFFF),
                0.35f to Color(0x38FFFFFF),
                0.73f to Color(0x16FFFFFF),
                1f to Color.Transparent,
                start = Offset.Zero,
                end = gradientEnd,
            )
            val reflection = Brush.linearGradient(
                0f to Color(0x1AFFFFFF),
                0.38f to Color(0x0EFFFFFF),
                0.75f to Color(0x04FFFFFF),
                1f to Color.Transparent,
                start = Offset.Zero,
                end = gradientEnd,
            )
            val edgeWidth = 0.75.dp.toPx()
            val reflectionWidth = 0.5.dp.toPx()
            val edgeInset = edgeWidth / 2f
            val reflectionInset = 1.5.dp.toPx() + reflectionWidth / 2f

            onDrawBehind {
                clipPath(clip) {
                    val backdropOrigin = backdrop?.origin ?: Offset.Unspecified
                    val layer = backdrop?.source
                    if (sample != null && layer != null && layer.size != IntSize.Zero &&
                        origin != Offset.Unspecified && backdropOrigin != Offset.Unspecified
                    ) {
                        val offset = origin - backdropOrigin
                        sample.record(size = bufferSize) {
                            translate(overscan - offset.x, overscan - offset.y) { drawLayer(layer) }
                        }
                        translate(-overscan.toFloat(), -overscan.toFloat()) { drawLayer(sample) }
                        drawRect(Color(0x1C0A0A0A))
                    } else {
                        drawRect(Color(0xD80A0A0A))
                    }
                    drawRect(tint)
                }
                drawRoundRect(
                    brush = edge,
                    topLeft = Offset(edgeInset, edgeInset),
                    size = Size(size.width - 2 * edgeInset, size.height - 2 * edgeInset),
                    cornerRadius = CornerRadius(radius - edgeInset),
                    style = Stroke(edgeWidth),
                )
                if (size.width > 2 * reflectionInset && size.height > 2 * reflectionInset) {
                    drawRoundRect(
                        brush = reflection,
                        topLeft = Offset(reflectionInset, reflectionInset),
                        size = Size(size.width - 2 * reflectionInset, size.height - 2 * reflectionInset),
                        cornerRadius = CornerRadius(radius - reflectionInset),
                        style = Stroke(reflectionWidth),
                    )
                }
            }
        }
}
