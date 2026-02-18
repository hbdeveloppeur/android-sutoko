package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "MessageImageComposable", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(14.dp)
    val rulesEnabled = false
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_message_image),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    ) {
                        MessageImageComposable(

                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
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
internal fun MessageImageComposable(modifier: Modifier = Modifier) {
    ImageBoxRow(
        url = "https://data.sutoko.app/resources/sutoko-ai/image/test-image.jpg",
        ratio = 0.7f
    ) {
        Avatar(url = "https://data.sutoko.app/resources/sutoko-ai/image/DefaultAvatar.jpg")
        Text(
            text = "01:00",
            color = Color.White.copy(1f),
            fontSize = 10.sp,
            letterSpacing = 0.2.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun Avatar(modifier: Modifier = Modifier, url: String) {
    AsyncImage(
        model = url, contentDescription = "Message avatar", modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            // .blur(2.dp)
            .border(1.dp, Color.White, CircleShape)
    )
}

@Composable
private fun ImageBoxRow(url: String, ratio: Float, content: @Composable RowScope.() -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D111B).copy(0f), Color(0xFF0D111B).copy(0.6f))
    )
    val shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp)
    Box(
        modifier = Modifier
            .height(200.dp)
            .aspectRatio(ratio)
            .clip(shape)
            .border(.5.dp, Color(0xFF94C6FB).copy(0.2f), shape)
    ) {
        AsyncImage(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .clipToBounds(),
            model =
            ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .background(gradient)
                .fillMaxSize()
        )

        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
            Spacer(modifier = Modifier.weight(1f))
        }

    }
}