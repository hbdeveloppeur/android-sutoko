package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.purpletear.sutoko.R

private val LogoWidth = 116.dp
private val LogoHeight = 48.dp
private val ArtworkLeadingInset = 2.35.dp
private const val SnowflakeCycleMillis = 2000
private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)
private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
private val EaseIn = CubicBezierEasing(0.42f, 0f, 1f, 1f)

// Centers of the two flakes in the shared artwork bounds.
private val SmallFlakeCenter = TransformOrigin(0.737f, 0.432f)
private val LargeFlakeCenter = TransformOrigin(0.874f, 0.234f)

@Composable
internal fun SnowflakeHeaderLogo(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.sutoko_logo_content_description)
    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = LogoWidth)
            .fillMaxWidth()
            .height(LogoHeight)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        val motion = rememberInfiniteTransition(label = "Snowflake drift")
        val smallFlakePhase = motion.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(keyframes {
                durationMillis = SnowflakeCycleMillis
                0f at 0 using EaseOut
                -1f at 450 using EaseInOut
                1f at 1350 using EaseIn
                0f at 1800
            }),
            label = "Small flake",
        )
        val largeFlakePhase = motion.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(keyframes {
                durationMillis = SnowflakeCycleMillis
                0f at 0 using EaseOut
                1f at 500 using EaseInOut
                -1f at 1500 using EaseIn
            }),
            label = "Large flake",
        )

        val logoScale = maxWidth / LogoWidth
        val flakeOffset = 3.35.dp * logoScale
        Box(
            Modifier
                .size(maxWidth, LogoHeight * logoScale)
                .offset(x = -ArtworkLeadingInset * logoScale),
        ) {
            SnowflakeArtworkLayer(
                artworkRes = R.drawable.sutoko_snowflake_wordmark,
                glowRes = R.drawable.sutoko_snowflake_wordmark_glow,
                modifier = Modifier.matchParentSize(),
            )
            SnowflakeArtworkLayer(
                artworkRes = R.drawable.sutoko_snowflake_small,
                glowRes = R.drawable.sutoko_snowflake_small_glow,
                modifier = Modifier.matchParentSize().graphicsLayer {
                    val drift = smallFlakePhase.value
                    transformOrigin = SmallFlakeCenter
                    rotationZ = 1.5f * drift
                    translationX = flakeOffset.toPx()
                    translationY = -(0.55.dp * logoScale).toPx() * drift
                },
            )
            SnowflakeArtworkLayer(
                artworkRes = R.drawable.sutoko_snowflake_large,
                glowRes = R.drawable.sutoko_snowflake_large_glow,
                modifier = Modifier.matchParentSize().graphicsLayer {
                    val drift = largeFlakePhase.value
                    transformOrigin = LargeFlakeCenter
                    rotationZ = 1.15f * drift
                    translationX = flakeOffset.toPx()
                    translationY = -(0.45.dp * logoScale).toPx() * drift
                },
            )
        }
    }
}

@Composable
private fun SnowflakeArtworkLayer(
    @DrawableRes artworkRes: Int,
    @DrawableRes glowRes: Int,
    modifier: Modifier,
) {
    Box(modifier) {
        Image(
            painter = painterResource(glowRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )
        Image(
            painter = painterResource(artworkRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )
    }
}
