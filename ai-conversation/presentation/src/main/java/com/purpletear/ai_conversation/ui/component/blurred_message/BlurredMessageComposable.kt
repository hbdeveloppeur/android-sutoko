package com.purpletear.ai_conversation.ui.component.blurred_message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


@Preview(name = "BlurredMessageComposable", showBackground = false, showSystemUi = false)
@Composable
private fun Preview() {
    Box {

        AsyncImage(
            model = "https://data.sutoko.app/resources/sutoko-ai/image/AiChatHomePageHeader.jpg",
            contentDescription = null
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            BlurredMessageComposable(
                color = Color(0xFFA3B2FD),
                cornersType = MessageCornerType.First
            )
            BlurredMessageComposable(
                color = Color(0xFFFDA3B4),
                cornersType = MessageCornerType.Middle
            )
            BlurredMessageComposable(
                color = Color(0xFFA9A3FD),
                cornersType = MessageCornerType.Last
            )
        }

    }
}

@Composable
private fun Avatar(modifier: Modifier = Modifier, url: String) {
    AsyncImage(
        model = url, contentDescription = "Message avatar", modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            // .blur(2.dp)
            .border(2.dp, Color.White, CircleShape)
    )
}

@Composable
internal fun BlurredMessageComposable(color: Color, cornersType: MessageCornerType) {


    val topStartCorner = when (cornersType) {
        MessageCornerType.First -> 24.dp
        MessageCornerType.Middle -> 18.dp
        MessageCornerType.Last -> 18.dp
    }

    val bottomStartCorner = when (cornersType) {
        MessageCornerType.First -> 14.dp
        MessageCornerType.Middle -> 18.dp
        MessageCornerType.Last -> 24.dp
    }

    val shape = RoundedCornerShape(
        topStart = topStartCorner,
        topEnd = 28.dp,
        bottomStart = bottomStartCorner,
        bottomEnd = 28.dp
    )

    Row(
        modifier = Modifier
            .scale(scaleX = -1f, scaleY = 1f)
            .background(color.copy(0.36f), shape = shape)
            .border(.5.dp, Color.White.copy(0.15f), shape)
            .padding(14.dp)
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = "https://data.sutoko.app/resources/sutoko-ai/image/DefaultAvatar.jpg",
            modifier = Modifier.alpha(0.6f)

        )
        Text(
            "Je ne cis pas" + " ",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .blur(4.dp),
            lineHeight = 30.sp,
        )
    }
}