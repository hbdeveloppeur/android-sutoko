package com.purpletear.game.presentation.game_play.components.message

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.SimpleButton
import com.purpletear.game.presentation.common.components.SimpleButtonIconSide
import kotlin.math.PI
import kotlin.math.sin

@Preview
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .aspectRatio(538f / 395f),
        drawable = R.drawable.preview_tmp_trial_finished,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(400.dp), contentAlignment = Alignment.Center
        ) {
            MessageChapterTrialFinished(gameLogoUrl = "")
        }
    }
}

@Composable
internal fun MessageChapterTrialFinished(
    modifier: Modifier = Modifier,
    gameLogoUrl: String,
    onClick: () -> Unit = {},
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Background()
        Content(
            title = stringResource(R.string.message_chapter_trial_finished_title),
            titleHighlight = stringResource(R.string.message_chapter_trial_finished_title_highlight),
            subtitle = stringResource(R.string.message_chapter_trial_finished_subtitle),
            subtitleHighlight = stringResource(R.string.message_chapter_trial_finished_subtitle_highlight),
            backText = stringResource(R.string.message_chapter_trial_finished_back_button),
            gameLogoUrl = gameLogoUrl,
            onClickBackButton = onClick,
        )
        Motifs()
    }
}

@Composable
private fun Content(
    title: String,
    titleHighlight: String,
    subtitle: String,
    subtitleHighlight: String,
    backText: String,
    gameLogoUrl: String,
    onClickBackButton: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Title(text = title, highlight = titleHighlight)
        GameLogo(url = gameLogoUrl)
        Subtitle(text = subtitle, highlight = subtitleHighlight)

        SimpleButton(
            text = backText,
            fontSize = 12.sp,
            onClick = onClickBackButton,
            horizontalPadding = 14.dp,
            verticalPadding = 6.dp,
            iconSide = SimpleButtonIconSide.Start,
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        )
    }
}

@Composable
private fun Background() {
    AsyncImage(
        modifier = Modifier
            .fillMaxSize(),
        model = R.drawable.gradient_blue_circle_bottom,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun GameLogo(url: String) {
    AsyncImage(
        modifier = Modifier
            .size(88.dp)
            .background(Color.Gray.copy(0.1f), shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun Title(text: String, highlight: String) {
    val annotated = remember(text, highlight) {
        highlightedAnnotatedString(text, highlight, MotifPink)
    }
    Text(
        text = annotated,
        color = Color.White,
        fontSize = 18.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Subtitle(text: String, highlight: String) {
    val annotated = remember(text, highlight) {
        highlightedAnnotatedString(text, highlight, MotifPink)
    }
    Text(
        text = annotated,
        color = Color.White,
        fontSize = 16.sp,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )
}

private fun highlightedAnnotatedString(
    text: String,
    highlight: String,
    color: Color
): AnnotatedString {
    if (highlight.isEmpty()) return AnnotatedString(text)
    val start = text.indexOf(highlight, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    val end = start + highlight.length
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(color = color)) { append(text.substring(start, end)) }
        append(text.substring(end))
    }
}

private val MotifPink = Color(0xFFFF3B8B)
private val MotifPurple = Color(0xFFB026FF)
private val MotifGray = Color(0xFF6B7280)

private const val MotifAnimPeriodMs = 6500
private const val MotifDriftDp = 3.5f
private const val MotifSwayDeg = 3f
private const val MotifPhaseStep = 0.17f
private const val MotifAlphaMin = 0.88f
private const val MotifAlphaPhase = 2.1f

private sealed class Motif(@DrawableRes val resId: Int) {
    data object Heart : Motif(R.drawable.motif_heart)
    data object Oct : Motif(R.drawable.motif_oct)
    data object Send : Motif(R.drawable.motif_send)
    data object Diamond : Motif(R.drawable.motif_diamond)
}

private data class MotifPlacement(
    val motif: Motif,
    val tint: Color?,
    val size: Dp,
    val rotation: Float,
    val xFraction: Float,
    val yFraction: Float,
    val opacity: Float = 1f,
)

private val MotifPlacements = listOf(
    MotifPlacement(Motif.Diamond, MotifPurple, 24.dp, -18f, 0.16f, 0.33f),
    MotifPlacement(Motif.Oct, MotifPurple, 26.dp, 14f, 0.85f, 0.34f, opacity = 0.3f),
    MotifPlacement(Motif.Send, null, 20.dp, -24f, 0.78f, 0.53f, opacity = 0.8f),
    MotifPlacement(Motif.Heart, MotifPink, 20.dp, -46f, 0.10f, 0.55f),
    MotifPlacement(Motif.Oct, MotifPurple, 24.dp, 24f, 0.92f, 0.80f),
    MotifPlacement(Motif.Oct, MotifGray, 22.dp, -12f, 0.11f, 0.88f),
)

@Composable
private fun Motifs() {

    // Session-stable: safe to drive conditional composition.
    val resolver = LocalContext.current.contentResolver
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
    val clock: State<Float>? = if (animationsEnabled) {
        rememberInfiniteTransition(label = "motifs").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(MotifAnimPeriodMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase",
        )
    } else {
        null
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        MotifPlacements.forEachIndexed { index, placement ->
            MotifIcon(placement = placement, index = index, clock = clock)
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.MotifIcon(
    placement: MotifPlacement,
    index: Int,
    clock: State<Float>?,
) {
    val baseX = maxWidth * placement.xFraction - placement.size / 2
    val baseY = maxHeight * placement.yFraction - placement.size / 2
    val driftPx = with(LocalDensity.current) { MotifDriftDp.dp.toPx() }
    AsyncImage(
        modifier = Modifier
            .offset(x = baseX, y = baseY)
            .size(placement.size)
            // Draw-only: animation never changes layout bounds or hit-testing.
            .graphicsLayer {
                val clockValue = clock?.value
                val angle = ((clockValue ?: 0f) + index * MotifPhaseStep) * 2f * PI.toFloat()
                if (clockValue != null) {
                    translationY = sin(angle) * driftPx
                    rotationZ = placement.rotation + sin(angle + 0.8f) * MotifSwayDeg
                    val alphaPulse = MotifAlphaMin + (1f - MotifAlphaMin) *
                            (0.5f + 0.5f * sin(angle + MotifAlphaPhase))
                    alpha = placement.opacity * alphaPulse
                } else {
                    rotationZ = placement.rotation
                    alpha = placement.opacity
                }
            },
        model = placement.motif.resId,
        contentDescription = null,
        colorFilter = placement.tint?.let { ColorFilter.tint(it) },
    )
}