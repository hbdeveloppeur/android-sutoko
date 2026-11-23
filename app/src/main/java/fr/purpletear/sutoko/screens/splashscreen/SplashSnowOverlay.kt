package fr.purpletear.sutoko.screens.splashscreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal const val SplashExitDurationMillis = 620
private const val SnowfallCycleSeconds = 6f
private const val FlakeLifetimeSeconds = 4.2f
private val FlakeColor = Color(0xFFD8F2FF)
private val Snowflakes = listOf(
    Snowflake(Offset(0.22f, 0.23f), startSeconds = -0.32f, radiusDp = 4.4f, phase = 2.4),
    Snowflake(Offset(0.49f, 0.50f), startSeconds = -0.40f, radiusDp = 6.5f, phase = 0.5),
    Snowflake(Offset(0.75f, 0.14f), startSeconds = 0.24f, radiusDp = 5.4f, phase = 4.0),
    Snowflake(Offset(0.12f, 0.63f), startSeconds = 0.52f, radiusDp = 4.2f, phase = 1.7),
    Snowflake(Offset(0.36f, 0.08f), startSeconds = 0.86f, radiusDp = 5.1f, phase = 0.2),
    Snowflake(Offset(0.69f, 0.72f), startSeconds = 1.22f, radiusDp = 4.8f, phase = 3.2),
    Snowflake(Offset(0.44f, 0.42f), startSeconds = 1.65f, radiusDp = 6.0f, phase = 2.7),
)

@Composable
internal fun SplashSnowOverlay(fadingOut: Boolean, onFadeFinished: () -> Unit) {
    val elapsedSeconds = rememberInfiniteTransition(label = "Splash snowfall").animateFloat(
        initialValue = 0f,
        targetValue = SnowfallCycleSeconds,
        animationSpec = infiniteRepeatable(tween((SnowfallCycleSeconds * 1_000).toInt(), easing = LinearEasing)),
        label = "Snowfall time",
    )
    val fade = remember { Animatable(0f) }
    val obscuresHome by remember { derivedStateOf { fade.value < 0.28f } }
    val finish by rememberUpdatedState(onFadeFinished)
    LaunchedEffect(fadingOut) {
        if (!fadingOut) return@LaunchedEffect
        try {
            // Let Home lay out under the black cover before starting the fade.
            withFrameNanos { }
            fade.animateTo(1f, tween(SplashExitDurationMillis, easing = LinearEasing))
        } finally {
            finish()
        }
    }

    val blockTouches = if (fadingOut && obscuresHome) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture { awaitFirstDown(requireUnconsumed = false).consume() }
        }
    } else Modifier

    Spacer(Modifier.fillMaxSize().then(blockTouches).drawWithCache {
        val path = snowflakePath()
        val stroke = Stroke(width = 0.17f, cap = StrokeCap.Round)
        val pixelsPerDp = 1.dp.toPx()

        onDrawBehind {
            val opacity = if (fadingOut) 1f - FastOutSlowInEasing.transform(fade.value) else 1f
            if (opacity <= 0f) return@onDrawBehind
            if (fadingOut) drawRect(Color.Black, alpha = opacity)

            val elapsed = elapsedSeconds.value
            for (flake in Snowflakes) {
                val age = (elapsed - flake.startSeconds + SnowfallCycleSeconds) % SnowfallCycleSeconds
                if (age >= FlakeLifetimeSeconds) continue

                val visibility = minOf(age / 0.28f, (FlakeLifetimeSeconds - age) / 0.55f, 1f)
                val sway = sin(age * 2 * PI / 1.6 + flake.phase).toFloat()
                val x = size.width * flake.origin.x + (14f * age + 9f * sway) * pixelsPerDp
                val y = size.height * flake.origin.y + (30f + flake.radiusDp * 1.5f) * age * pixelsPerDp
                val radius = flake.radiusDp * pixelsPerDp
                withTransform({
                    translate(x, y)
                    rotate(flake.phase.toFloat() * 36f + 17f * sway, pivot = Offset.Zero)
                    scale(radius, radius, pivot = Offset.Zero)
                }) {
                    drawPath(path, FlakeColor, alpha = 0.78f * visibility * opacity, style = stroke)
                }
            }
        }
    })
}

private data class Snowflake(
    val origin: Offset,
    val startSeconds: Float,
    val radiusDp: Float,
    val phase: Double,
)

private fun snowflakePath() = Path().apply {
    repeat(6) { arm ->
        val angle = arm * PI / 3
        val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        val side = Offset(-direction.y, direction.x)
        val fork = direction * 0.56f
        val left = direction * 0.8f + side * 0.23f
        val right = direction * 0.8f - side * 0.23f

        moveTo(0f, 0f)
        lineTo(direction.x, direction.y)
        moveTo(left.x, left.y)
        lineTo(fork.x, fork.y)
        lineTo(right.x, right.y)
    }
}
