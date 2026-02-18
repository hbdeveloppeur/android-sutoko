package com.purpletear.ai_conversation.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.gigamole.composeshadowsplus.common.shadowsPlus
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme

@Composable
@Preview(name = "ButtonComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_button),
                contentDescription = null,
            )
            ButtonComposable(
                title = "Discuss with Eva",
                subtitle = "Eva is the most beautiful",
                theme = ButtonTheme.Pink(glow = true)
            )
            Image(
                painter = painterResource(id = R.drawable.preview_button_buy),
                contentDescription = null,
            )
            ButtonComposable(
                title = "Buy more messages",
                theme = ButtonTheme.Maroon()
            )
            Image(
                painter = painterResource(id = R.drawable.preview_short_button),
                contentDescription = null,
            )
            Box(
                Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShortButtonComposable(
                    modifier = Modifier
                        .padding(end = 34.dp)
                        .height(48.dp),
                    title = "Buy coins",
                    isEnabled = true,
                    onClick = {}
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                WhitePillArrowComposable(
                    modifier = Modifier.padding(end = 34.dp),
                    title = "Demo",
                    isEnabled = true,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
internal fun ButtonComposable(
    modifier: Modifier = Modifier,
    theme: ButtonTheme,
    title: String,
    isEnabled: Boolean = true,
    subtitle: String? = null,
    isLoading: Boolean = false,
    onClick: () -> Unit = {
        
    }
) {

    val onclick = f@{
        if (isLoading) {
            return@f
        }
        onClick()
    }

    when (theme) {
        is ButtonTheme.Pink, is ButtonTheme.Maroon -> LongButtonComposable(
            modifier = modifier,
            theme = theme,
            title = title,
            subtitle = subtitle,
            isLoading = isLoading,
            isEnabled = isEnabled,
            onClick = onclick,
        )

        is ButtonTheme.WhitePillArrow -> WhitePillArrowComposable(
            modifier = modifier,
            title = title,
            isEnabled = isEnabled,
            onClick = onClick
        )

        is ButtonTheme.WhitePill -> ShortButtonComposable(
            modifier = modifier, title = title,
            iconId = theme.iconId,
            isEnabled = isEnabled,
            onClick = onClick
        )
    }

}


@Composable
internal fun ShortButtonComposable(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    title: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .height(34.dp)
            .wrapContentWidth()
            .then(modifier)
            .clickable(
                enabled = isEnabled,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = ripple(),
                onClick = onClick
            )
            .background(Color.White, shape = RoundedCornerShape(50))
            .padding(end = 18.dp, start = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconId?.let { iconId ->
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape),
                painter = painterResource(id = iconId),
                contentDescription = null,
            )
        }
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Black)
    }
}


@Composable
internal fun WhitePillArrowComposable(
    modifier: Modifier = Modifier,
    title: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .height(34.dp)
            .wrapContentWidth()
            .then(modifier)
            .background(Color.White, shape = RoundedCornerShape(50))
            .clickable(
                enabled = isEnabled,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = ripple(),
                onClick = onClick
            )
            .padding(end = 14.dp, start = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Black)
        Image(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape),
            painter = painterResource(id = R.drawable.line_arrow_right),
            contentDescription = null,
        )
    }
}


@Composable
private fun LongButtonComposable(
    modifier: Modifier = Modifier,
    theme: ButtonTheme,
    title: String,
    subtitle: String? = null,
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit = {}
) {

    val colors = when (theme) {
        is ButtonTheme.Pink -> listOf(Color(0xFFFF01AA), Color(0xFFFD4982))
        is ButtonTheme.Maroon -> listOf(Color(0xFF3D2F45), Color(0xFF4B342F))
        else -> throw IllegalArgumentException("Invalid button theme")
    }

    val s = MaterialTheme.shapes.small

    val glowModifier = when (theme) {
        is ButtonTheme.Pink -> if (theme.glow) {
            Modifier.shadowsPlus(
                radius = 12.dp,
                color = Color(0xFFFF00AB).copy(0.6f),
                shape = s,
                spread = 0.dp,
                offset = DpOffset(0.dp, 0.dp),
                isAlphaContentClip = true
            )
        } else {
            Modifier
        }

        is ButtonTheme.Maroon -> Modifier
        else -> throw IllegalArgumentException("Invalid button theme")
    }

    Box(
        Modifier
            .then(glowModifier)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = colors
                    ), shape = MaterialTheme.shapes.small
                )
                .clickable(
                    enabled = isEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick,
                    indication = ripple(),
                )
                .alpha(if (isLoading) 0.3f else 1f)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val offset = Offset(1.0f, 1.0f)
                Text(
                    text = title,
                    modifier = modifier,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium, shadow = Shadow(
                            color = Color.Black.copy(0.2f), offset = offset, blurRadius = 3f
                        )
                    ),
                    color = Color.White
                )

                subtitle?.let {
                    Text(
                        text = subtitle,
                        modifier = modifier,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {

                if (theme is ButtonTheme.Pink) {
                    val vector = ImageVector.vectorResource(id = R.drawable.arrow_right)
                    val painter = rememberVectorPainter(image = vector)
                    val tintColor = Color.White.copy(0.3f)


                    Image(
                        painter = painter,
                        contentDescription = "",
                        modifier = Modifier.height(56.dp),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
                    )
                } else {
                    val vector = ImageVector.vectorResource(id = R.drawable.theme_crosses)
                    val painter = rememberVectorPainter(image = vector)
                    val tintColor = Color.White.copy(0.2f)


                    Image(
                        painter = painter,
                        contentDescription = "",
                        modifier = Modifier
                            .height(56.dp)
                            .align(Alignment.Center)
                            .alpha(0.4f)
                            .graphicsLayer {
                                scaleX = 1.1f
                                scaleY = 1.1f
                            },
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
                    )
                }
            }

            theme.iconId?.let { drawableId ->
                val vector = ImageVector.vectorResource(id = drawableId)
                val painter = rememberVectorPainter(image = vector)
                val tintColor = Color.White


                Image(
                    painter = painter,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(end = 28.dp)
                        .size(18.dp)
                        .align(Alignment.CenterEnd),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
                )
            }

        }
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center),
                    color = Color.LightGray,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}