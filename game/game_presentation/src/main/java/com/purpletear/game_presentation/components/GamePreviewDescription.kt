package com.purpletear.game_presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
internal fun GamePreviewDescription(
    modifier: Modifier = Modifier,
    avatarUrl: String,
    description: String
) {
    val textMeasurer = rememberTextMeasurer()
    val localDensity = LocalDensity.current
    var descriptionWidth by remember { mutableIntStateOf(1000) }
    var descriptionAlpha by remember { mutableFloatStateOf(0f) }

    // Calculate target height based on text measurement
    val constraints = Constraints(maxWidth = descriptionWidth) // Adjust based on your layout
    val measuredText = textMeasurer.measure(
        text = buildColoredAnnotatedString(description),
        style = TextStyle(
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily(
                Font(
                    com.example.sharedelements.R.font.font_worksans_regular,
                    FontWeight.Normal
                )
            )
        ),
        constraints = constraints
    )

    LaunchedEffect(description) {
        descriptionAlpha = 1f
    }

    // Convert height to dp and add some padding
    val targetHeight = with(localDensity) { measuredText.size.height.toDp() + 10.dp }

    // Animated height to smooth transition
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "AnimatedHeight"
    )

    // Animated alpha to smooth transition
    val animatedAlpha by animateFloatAsState(
        targetValue = descriptionAlpha,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
        label = "AnimatedAlpha"
    )

    Box(modifier = Modifier.height(animatedHeight)) {

        Column {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {

                Avatar(
                    url = avatarUrl,
                    totalHeight = animatedHeight,
                    minHeight = 22.dp,
                )

                Box(
                    Modifier
                        .heightIn(min = 22.dp)
                        .height(animatedHeight)
                        .onGloballyPositioned {
                            descriptionWidth = it.size.width
                        }
                ) {
                    AnnotatedText(
                        modifier = Modifier.graphicsLayer {
                            alpha = animatedAlpha
                        },
                        text = description,
                    )
                }
            }
        }
    }
}

@Composable
private fun Avatar(url: String, totalHeight: Dp, minHeight: Dp = 20.dp) {
    val uri = url.toUri()
    val context = LocalContext.current
    val imageSize = 22.dp
    val lineHeight = if (totalHeight > minHeight) totalHeight - imageSize else minHeight - imageSize

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Image with memory leak prevention
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imageSize)
                .clip(
                    RoundedCornerShape(4.dp)
                )
                .border(0.75.dp, Color.White, RoundedCornerShape(4.dp))
                .background(Color.White)
        )

        // White line below the image
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(0.5.dp)
                .height(lineHeight)
                .background(Color.White.copy(0.3f))
        )
    }

    // Clean up resources when the composable is disposed
    DisposableEffect(uri) {
        onDispose {
            // This will be called when the composable leaves the composition
            // Coil handles cleanup automatically, but this is a good practice
        }
    }
}

@Composable
private fun AnnotatedText(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier
            .graphicsLayer {
                translationY = -1.dp.toPx()
            },
        maxLines = Int.MAX_VALUE,
        text = buildColoredAnnotatedString(text),
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Color(0xFFFFFFFF),
        textAlign = TextAlign.Justify,
        fontFamily = FontFamily(
            Font(
                com.example.sharedelements.R.font.font_worksans_regular,
                FontWeight.Normal
            )
        ),
    )
}

private fun buildColoredAnnotatedString(
    text: String,
    color: Color = Color(0xFFFAD7FF)
): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("\\*".toRegex())
        if (parts.size % 2 != 0) {
            for (i in parts.indices) {
                if (i % 2 == 0) {
                    append(parts[i])
                } else {
                    withStyle(
                        style = SpanStyle(
                            color = color,
                            fontFamily = FontFamily(
                                Font(
                                    com.example.sharedelements.R.font.font_worksans_semibold,
                                    FontWeight.SemiBold
                                )
                            )
                        )
                    ) {
                        append(parts[i])
                    }
                }
            }
        } else {
            append(text)
        }
    }
}
