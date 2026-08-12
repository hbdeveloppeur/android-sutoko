package com.purpletear.game.presentation.game_play.components.visual_novel

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.CrimsonTextFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.extensions.parseOrNull
import com.purpletear.game.presentation.game_play.components.background.VideoBackground
import com.purpletear.game.presentation.game_play.state.VisualNovelUi
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.delay

private const val SCRIM_ALPHA = 0.5f
private const val MAX_WIDTH_FRACTION = 0.94f
private const val MAX_HEIGHT_FRACTION = 0.75f

// Slightly above vertical center so the card feels higher on screen.
private const val CARD_VERTICAL_BIAS = -0.15f
private const val FRAME_ASPECT = 3f / 2f
private const val DIALOG_FADE_MS = 300

// Beat on an empty dialog area between two dialogs, after the fade-out completes.
private const val DIALOG_GAP_MS = 100L
private const val CONTINUE_FADE_IN_MS = 600
private const val DISMISS_MIN_DELAY_MS = 8_000L
private val CornerRadius = 12.dp
private val FallbackThemeColor = Color(0xFF332F63)
private val UnspokenWordColor = Color.White.copy(alpha = 0.35f)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun VisualNovelOverlay(
    visualNovel: VisualNovelUi,
    onDismiss: () -> Unit,
    onDialogSound: (String) -> Unit,
) {
    if (visualNovel.layers.isEmpty()) return

    BackHandler(enabled = true) { onDismiss() }

    val context = LocalContext.current
    val themeColor = remember(visualNovel.themeColorHex) {
        Color.parseOrNull(visualNovel.themeColorHex) ?: FallbackThemeColor
    }
    // The theme colors sit on a dark scene: a raw #332F63 title would be unreadable, so the
    // title uses the theme color lightened toward white (pure black theme falls back to white).
    val titleColor = remember(themeColor) {
        if (themeColor == Color.Black) Color.White else lerp(themeColor, Color.White, 0.62f)
    }

    // The continue button fades in once the dialogs are done AND at least DISMISS_MIN_DELAY_MS
    // has elapsed: it must never appear earlier, whatever the authored dialog durations.
    var dialogsFinished by remember(visualNovel) { mutableStateOf(false) }
    var minDelayElapsed by remember(visualNovel) { mutableStateOf(false) }
    LaunchedEffect(visualNovel) {
        delay(DISMISS_MIN_DELAY_MS)
        minDelayElapsed = true
    }
    val showContinue = dialogsFinished && minDelayElapsed

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                // Tapping outside the card dismisses the overlay, under the same conditions as
                // the continue button (dialogs done AND minimum display delay elapsed).
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = showContinue,
                    onClick = onDismiss
                )
        )

        BoxWithConstraints(
            Modifier.align(BiasAlignment(0f, CARD_VERTICAL_BIAS)),
            content = {
                val maxW = maxWidth * MAX_WIDTH_FRACTION
                val maxH = maxHeight * MAX_HEIGHT_FRACTION
                val frameWidth: Dp
                val frameHeight: Dp
                if (maxW / FRAME_ASPECT <= maxH) {
                    frameWidth = maxW
                    frameHeight = maxW / FRAME_ASPECT
                } else {
                    frameHeight = maxH
                    frameWidth = maxH * FRAME_ASPECT
                }

                Box(
                    Modifier
                        .size(frameWidth, frameHeight)
                        .clip(RoundedCornerShape(CornerRadius))
                        .background(Color.Black)
                ) {
                    // Superposed layers, first in the list at the bottom.
                    visualNovel.layers.forEach { layer ->
                        if (layer.isVideo) {
                            VideoBackground(
                                videoPath = layer.path,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(layer.path)
                                    .crossfade(300)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    // Bottom violet gradient behind the title and dialogs.
                    Image(
                        painter = painterResource(R.drawable.game_presentation_gradient_violet),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxSize(),
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        visualNovel.title?.let { title ->
                            Text(
                                text = title,
                                color = titleColor,
                                fontFamily = CrimsonTextFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color(0xFFFF58E3).copy(alpha = 0.8f),
                                        offset = Offset.Zero,
                                        blurRadius = 20f,
                                    )
                                ),
                            )
                        }

                        DialogText(
                            visualNovel = visualNovel,
                            onDialogSound = onDialogSound,
                            onFinished = { dialogsFinished = true },
                        )
                    }
                }
            })

        // "Continuer >>" under the card, fading in once the sequence is over (same affordance
        // as the cinematic "Skip >>": dim white text, no ripple, generous touch padding).
        AnimatedVisibility(
            visible = showContinue,
            enter = fadeIn(tween(CONTINUE_FADE_IN_MS)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Text(
                text = stringResource(R.string.game_presentation_visual_novel_continue),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier
                    .testTag("visual_novel_continue")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
                    .padding(horizontal = 32.dp, vertical = 42.dp),
            )
        }
    }
}

/**
 * Cycles through the dialogs: each one appears after its authored delay, holds for its authored
 * duration, fades out fully, then the next fades in after a short beat on an empty dialog area
 * (sequential fades: an overlapping cross-fade reads as a visual glitch). The last dialog holds
 * until the overlay is dismissed. A dialog carrying a sound fires [onDialogSound] when it
 * appears, and its words light up one after another over its duration. [onFinished] fires once
 * the last dialog is reached.
 */
@Composable
private fun DialogText(
    visualNovel: VisualNovelUi,
    onDialogSound: (String) -> Unit,
    onFinished: () -> Unit,
) {
    val dialogs = visualNovel.dialogs
    if (dialogs.isEmpty()) {
        // No dialogs: nothing to wait for, the overlay's minimum display delay still applies.
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    // -1 means nothing is shown: before the first dialog's delay, and during the beat
    // between two dialogs while the previous one fades out.
    var dialogIndex by remember(visualNovel) { mutableIntStateOf(-1) }
    LaunchedEffect(dialogs) {
        for (index in dialogs.indices) {
            delay(dialogs[index].delayMs)
            dialogIndex = index
            dialogs[index].soundPath?.let(onDialogSound)
            if (index == dialogs.lastIndex) break
            val duration = dialogs[index].durationMs ?: break
            delay(duration)
            // Fade the current dialog out fully and hold a beat before the next one appears.
            dialogIndex = -1
            delay(DIALOG_FADE_MS + DIALOG_GAP_MS)
        }
        if (dialogIndex == dialogs.lastIndex) onFinished()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Invisible copies of every dialog reserve the height of the tallest one, so
        // fading between dialogs of different lengths never shifts the layout.
        dialogs.forEach { dialog ->
            Text(
                text = dialog.text,
                fontFamily = CrimsonTextFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(0f),
            )
        }
        Crossfade(
            targetState = dialogIndex,
            animationSpec = tween(DIALOG_FADE_MS),
            label = "visualNovelDialog",
        ) { index ->
            if (index >= 0) {
                DialogBody(dialogs[index])
            }
        }
    }
}

/**
 * One dialog line. When the dialog carries a sound, its words light up one after another
 * (karaoke-style) over the dialog duration; otherwise the whole line is shown lit at once.
 */
@Composable
private fun DialogBody(dialog: Node.VisualNovel.Dialog) {
    val highlightDurationMs = dialog.durationMs?.takeIf { dialog.soundPath != null && it > 0 }

    val text = if (highlightDurationMs == null) {
        AnnotatedString(dialog.text)
    } else {
        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(highlightDurationMs.toInt(), easing = LinearEasing),
            )
        }
        highlightedWords(dialog.text, progress.value)
    }

    Text(
        text = text,
        color = Color.White,
        fontFamily = CrimsonTextFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp),
    )
}

/** [text] with the already-spoken words lit white and the rest dimmed, snapped to word ends. */
private fun highlightedWords(text: String, progress: Float): AnnotatedString {
    val litChars = (progress.coerceIn(0f, 1f) * text.length).toInt()
    // Snap to the end of the word being spoken so highlighting moves word by word.
    val boundary = when {
        litChars <= 0 -> 0
        litChars >= text.length -> text.length
        else -> text.indexOf(' ', litChars).let { if (it < 0) text.length else it }
    }
    return buildAnnotatedString {
        withStyle(SpanStyle(color = Color.White)) { append(text.take(boundary)) }
        withStyle(SpanStyle(color = UnspokenWordColor)) { append(text.drop(boundary)) }
    }
}

