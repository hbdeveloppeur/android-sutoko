package com.purpletear.aiconversation.presentation.screens.home.components.notice

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme
import com.purpletear.aiconversation.presentation.theme.SubTitleColor

private val OrangeColor = Color(0xFFFF8A3D)
private val BorderBaseColor = Color.White.copy(alpha = 0.08f)

@Composable
fun AiFriendShutdownNoticeComposable(
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    // Slow shimmer sliding along the border, drawn in draw phase (no recomposition per frame).
    val infiniteTransition = rememberInfiniteTransition(label = "Border shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Border shimmer progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF14161B), shape)
            .drawWithContent {
                drawContent()
                val bandWidth = size.width * 0.6f
                val travel = size.width + bandWidth * 2
                val startX = -bandWidth + shimmerProgress * travel
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to BorderBaseColor,
                            0.5f to OrangeColor.copy(alpha = 0.55f),
                            1.0f to BorderBaseColor
                        ),
                        start = Offset(startX, 0f),
                        end = Offset(startX + bandWidth, size.height)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.ai_conversation_shutdown_notice_tag),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            ),
            color = OrangeColor
        )
        Text(
            text = stringResource(R.string.ai_conversation_shutdown_notice_title),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White
        )
        Text(
            text = stringResource(R.string.ai_conversation_shutdown_notice_message),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                lineHeight = 17.sp
            ),
            color = SubTitleColor
        )
    }
}

@Preview(name = "AiFriendShutdownNotice", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun Preview() {
    AiConversationTheme {
        AiFriendShutdownNoticeComposable(Modifier.padding(16.dp))
    }
}
