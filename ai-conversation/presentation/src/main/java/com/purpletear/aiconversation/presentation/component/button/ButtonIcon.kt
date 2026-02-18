package com.purpletear.aiconversation.presentation.component.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "ButtonIconComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_full_image_card),
                contentDescription = null,
            )
            Row(
                Modifier
                    .fillMaxWidth(0.86f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ButtonIconComposable(
                    Modifier.weight(1f),
                    drawableId = R.drawable.vec_image,
                    text = "Import"
                )
                ButtonIconComposable(
                    Modifier.weight(1f),
                    drawableId = R.drawable.vec_random,
                    text = "Random"
                )
                ButtonIconComposable(
                    Modifier.weight(1f),
                    drawableId = R.drawable.vec_magic,
                    text = "Use IA",
                    isPremium = true
                )
            }
        }
    }
}

@Composable
fun ButtonIconComposable(
    modifier: Modifier = Modifier,
    text: String,
    drawableId: Int,
    isPremium: Boolean = false,
    onClick: () -> Unit = {}
) {
    val gradientBrush = Brush.linearGradient(
        colors = if (isPremium)
            listOf(Color(0xFFFF21D1), Color(0xFFD0D31F))
        else
            listOf(Color(0xFF537399), Color(0xFF537399)),
    )

    Box(
        modifier
            .height(72.dp)
            .widthIn(min = 88.dp)
            .border(1.dp, gradientBrush, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF151923))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),

            ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = drawableId),
                contentDescription = "Magic Icon",
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}