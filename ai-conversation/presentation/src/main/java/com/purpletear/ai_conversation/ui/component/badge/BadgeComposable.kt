package com.purpletear.ai_conversation.ui.component.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme


@Preview(name = "BadgeComposablePreview", showBackground = false, showSystemUi = false)
@Composable
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color(0xFF161826)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_badge),
                contentDescription = null,
            )

            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BadgeComposable(text = "Realistic")
                BadgeComposable(text = "K-Drama")
                BadgeComposable(text = "Manga")
            }
        }
    }
}

@Composable
internal fun BadgeComposable(text: String) {
    val borderBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBAC7E1).copy(0.45f), Color(0xFFBBCBE7).copy(0.35f))
    )

    val verticalPadding = 6.dp
    val horizontalPadding = verticalPadding * 3
    Box(
        modifier = Modifier
            .border(
                width = (0.5).dp,
                brush = borderBrush,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}