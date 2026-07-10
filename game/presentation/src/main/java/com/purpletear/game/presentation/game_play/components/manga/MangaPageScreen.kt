package com.purpletear.game.presentation.game_play.components.manga

import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.sutoko.game.engine.message.GameMessageMangaPage
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

private const val DEFAULT_ASPECT = 0.7f // width / height, portrait fallback until the image loads
private const val LETTER_SPACING_EM = 0.1f // matches legacy MangaHelper

/**
 * Full-screen overlay that displays a manga page with its speech-bubble [overlays]
 * drawn on top. Despite the name, this is an **in-screen overlay**, not a Nav
 * destination: it is rendered by `SmsGameScreen` and dismissed via [onDismiss]
 * (back, tap outside, or the Close button).
 *
 * Rendering notes (see docs/node-manga-page-implementation.md):
 * - The image is loaded with Coil and never mutated; text is drawn with Compose.
 * - Overlays are positioned in image-relative percent space and scaled by the
 *   image intrinsic -> displayed ratio, so they stay glued to the page under zoom.
 * - [StaticLayout]s and the manga [Typeface] are built once per (overlays, page size)
 *   and cached, so pinch-zoom never rebuilds them.
 */
@Composable
internal fun MangaPageScreen(
    imageUrl: String?,
    overlays: List<GameMessageMangaPage.TextOverlay>,
    isVisible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!isVisible || imageUrl.isNullOrBlank()) return

    BackHandler(enabled = true) { onDismiss() }

    val zoomState = rememberZoomState()
    var intrinsicAspect by remember { mutableStateOf<Float?>(null) }
    var intrinsicWidth by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss,
            )
            .zoomable(zoomState),
        contentAlignment = Alignment.Center,
    ) {
        PageBox(
            imageUrl = imageUrl,
            overlays = overlays,
            aspect = intrinsicAspect ?: DEFAULT_ASPECT,
            intrinsicWidth = intrinsicWidth,
            onImageLoaded = { width, aspect ->
                intrinsicWidth = width
                intrinsicAspect = aspect
            },
        )

        CloseButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun BoxScope.PageBox(
    imageUrl: String,
    overlays: List<GameMessageMangaPage.TextOverlay>,
    aspect: Float,
    intrinsicWidth: Float?,
    onImageLoaded: (widthPx: Float, aspect: Float) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val typeface = rememberMangaTypeface()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect),
        contentAlignment = Alignment.Center,
    ) {
        val pageWidthPx = with(density) { maxWidth.toPx() }
        val pageHeightPx = with(density) { maxHeight.toPx() }

        val layouts = remember(overlays, pageWidthPx, pageHeightPx, intrinsicWidth, typeface) {
            buildOverlayLayouts(
                overlays = overlays,
                pageWidth = pageWidthPx,
                pageHeight = pageHeightPx,
                intrinsicWidth = intrinsicWidth,
                typeface = typeface,
            )
        }

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(300)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.width.isFinite() && size.height.isFinite() && size.width > 0f && size.height > 0f) {
                    onImageLoaded(size.width, size.width / size.height)
                }
            },
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                layouts.forEach { entry ->
                    canvas.save()
                    canvas.translate(entry.x, entry.y)
                    entry.layout.draw(canvas.nativeCanvas)
                    canvas.restore()
                }
            }
        }
    }
}

@Composable
private fun CloseButton(modifier: Modifier, onClick: () -> Unit) {
    Text(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(8.dp),
        text = "Close",
        color = Color.White,
    )
}

@Composable
private fun rememberMangaTypeface(): Typeface {
    val context = LocalContext.current
    return remember {
        runCatching { Typeface.createFromAsset(context.assets, "fonts/manga.ttf") }
            .getOrElse { Typeface.DEFAULT }
    }
}

private data class OverlayLayout(
    val x: Float,
    val y: Float,
    val layout: StaticLayout,
)

/**
 * Builds one [StaticLayout] per overlay. Coordinates reproduce the legacy mapping
 * (MangaHelper.drawText): x/y are the text center in % of the page, w is the
 * constrained text width in % of the page width, and size (in image pixels) is
 * scaled by the displayed-page / intrinsic-image ratio.
 */
private fun buildOverlayLayouts(
    overlays: List<GameMessageMangaPage.TextOverlay>,
    pageWidth: Float,
    pageHeight: Float,
    intrinsicWidth: Float?,
    typeface: Typeface,
): List<OverlayLayout> {
    if (pageWidth <= 0f || pageHeight <= 0f) return emptyList()

    val sizeScale = if (intrinsicWidth != null && intrinsicWidth > 0f) {
        pageWidth / intrinsicWidth
    } else {
        1f
    }

    return overlays.map { overlay ->
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.BLACK
            textSize = overlay.size * sizeScale
            this.typeface = typeface
            letterSpacing = LETTER_SPACING_EM
        }
        val constrainedWidth = (overlay.w / 100f * pageWidth).toInt().coerceAtLeast(1)
        val layout = StaticLayout.Builder
            .obtain(overlay.text, 0, overlay.text.length, paint, constrainedWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        val x = overlay.x / 100f * pageWidth - layout.width / 2f
        val y = overlay.y / 100f * pageHeight - layout.height / 2f
        OverlayLayout(x = x, y = y, layout = layout)
    }
}
