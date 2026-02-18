package com.purpletear.ai_conversation.ui.component.options

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.core.presentation.services.performVibration
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme
import com.purpletear.ai_conversation.ui.theme.BlueBackground
import com.purpletear.ai_conversation.ui.theme.FlashyPinkColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview(name = "QuotedTextComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_circular_options),
                contentDescription = null,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularOption(
                    text = "Realistic",
                    isSelected = false
                )
                CircularOption(
                    text = "Manga",
                    isSelected = true
                )
                CircularOption(
                    text = "K-POP",
                    isSelected = false
                )
                CircularOption(
                    text = "Futuristic",
                    isSelected = true
                )
                CircularOption(
                    text = "Magic",
                    isSelected = false
                )
                CircularOption(
                    text = "Vampire romance",
                    isSelected = false
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CircularOption(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    val animationSpec = tween<Color>(
        durationMillis = 280, // Duration of 1000 milliseconds (1 second)
        easing = FastOutSlowInEasing // Easing function for a smoother effect
    )
    val context = LocalContext.current

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FlashyPinkColor else BlueBackground,
        animationSpec = animationSpec,
        label = "backgroundColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else Color(0xFF5B6585),
        animationSpec = animationSpec,
        label = "borderColor"
    )

    Box(
        Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .then(modifier)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    performVibration(context)
                    onClick()
                }
            )
            .padding(horizontal = 22.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
