package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.aiconversation.presentation.R


@Composable
fun CallToActionButtonComposable(
    title: String,
    titleIcon: Int? = null,
    subtitle: String?,
    backgroundColor: Color = Color(0xFF1D2024),
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .height(52.dp)
            .width(145.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 13.sp,
                maxLines = 1
            )
            titleIcon?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically)
                        .size(16.dp)
                )
            }
        }
        subtitle?.let {
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

enum class ButtonStyle {
    VIOLET,
    PINK,
    WHITE,
    GOLD
}

@Composable
fun CallToActionRowButtonComposable(
    title: String,
    titleIcon: Int? = null,
    subtitle: String,
    onClick: () -> Unit = {},
    style: ButtonStyle = ButtonStyle.PINK
) {
    val colors = when (style) {
        ButtonStyle.PINK -> {
            listOf(
                Color(0xFFFF3797),
                Color(0xFFFF087F)
            )
        }

        ButtonStyle.VIOLET -> {
            listOf(
                Color(0xFFF837F5),
                Color(0xFFA2199F)
            )
        }

        ButtonStyle.WHITE, ButtonStyle.GOLD -> {
            listOf(
                Color(0xFF202027),
                Color(0xFF2E2E38),
            )
        }
    }
    Box {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = colors
                    )
                )
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        titleIcon?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 18.dp)
                    .align(Alignment.CenterEnd)
                    .size(20.dp)
            )
        }

        if (style == ButtonStyle.GOLD) {
            AnimationStarsExplode(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun AnimationStarsExplode(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_stars_explode))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    Box(
        modifier = modifier
            .height(52.dp)
            .width(86.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            modifier = Modifier
                .matchParentSize()
                .offset(x = (13).dp)
                .graphicsLayer(
                    scaleX = 4f,
                    scaleY = 4f
                )
                .alpha(0.4f)
                .align(Alignment.Center),
            composition = composition,
            progress = { progress },
        )
    }
}

@Preview
@Composable
private fun CallToActionButtonComposablePreview() {
    Box(
        modifier = Modifier
            .background(Color(0xFF04070C))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            CallToActionButtonComposable(
                title = "Try for 100",
                titleIcon = R.drawable.ai_conversation_presentation_item_coin,
                subtitle = "100 messages"
            )
            CallToActionButtonComposable(
                title = "Buy for 900",
                titleIcon = R.drawable.ai_conversation_presentation_item_coin,
                subtitle = "1000 messages",
                backgroundColor = Color(0xFFFF007A)
            )
        }
    }
}