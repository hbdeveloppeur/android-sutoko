package com.purpletear.ai_conversation.ui.component.tool_button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme
import com.purpletear.ai_conversation.ui.theme.FlashyPinkColor
import com.purpletear.ai_conversation.ui.theme.FlashyPinkDisabledColor
import com.purpletear.ai_conversation.ui.theme.Shapes


@Composable
@Preview(name = "ToolButtonComposable", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(16.dp, 336.dp)
    val rulesEnabled = true
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_tool_button),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    ToolButtonComposable(
                        text = "Use Image as your character avatar",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    )
                }
            }
            if (rulesEnabled) {
                verticalRules.forEach { startPadding ->
                    Box(
                        Modifier
                            .padding(start = startPadding)
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ToolButtonComposable(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean = true,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {}
) {

    val clickableModifier = if (isLoading || !isEnabled) {
        Modifier
    } else {
        Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) {
                onClick()
            }
    }

    val whiteColorAlpha = if (isLoading || !isEnabled) {
        0.3f
    } else {
        1f
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(Shapes.extraSmall)
            .background(if (isLoading || !isEnabled) FlashyPinkDisabledColor.copy(whiteColorAlpha) else FlashyPinkColor)
            .then(clickableModifier), contentAlignment = Alignment.CenterEnd
    ) {

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = whiteColorAlpha),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}