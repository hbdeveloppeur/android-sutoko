package com.purpletear.aiconversation.presentation.component.image_action_card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.buildColoredAnnotatedString
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme
import com.purpletear.aiconversation.presentation.theme.LoadingColor

@Composable
@Preview(name = "ImageActionCardComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_image_action_card),
                contentDescription = null,
            )
            ImageActionCardComposable(
                Modifier.fillMaxWidth(0.92f),
                title = "Get coins\nand *play*",
                url = "https://data.sutoko.app/resources/sutoko-ai/image/coins_snow_holo.jpg",
                onClick = {}
            )
        }
    }
}

@Composable
fun ImageActionCardComposable(
    modifier: Modifier = Modifier,
    url: String,
    title: String,
    onClick: () -> Unit,
) {

    Box(
        modifier
            .height(135.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(LoadingColor)
    ) {

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.618f)
                .align(Alignment.CenterEnd)
                .alpha(0.8f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0x005861E9), Color(0xFF3C406A))
                    ), shape = MaterialTheme.shapes.small
                )
        )

        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .align(Alignment.CenterEnd)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0xFF5837EC).copy(0.6f)),
                    ),
                )
        )

        Column(
            Modifier
                .width(200.dp)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,

            ) {
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = buildColoredAnnotatedString(title, Color(0xFFFF7EC6)),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 26.sp),
                color = Color.White,
            )

            Spacer(Modifier.weight(1f))

            Box(
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable(
                        onClick = onClick
                    )
            ) {
                Text(
                    stringResource(R.string.ai_conversation_open),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black
                )
            }
        }
    }
}