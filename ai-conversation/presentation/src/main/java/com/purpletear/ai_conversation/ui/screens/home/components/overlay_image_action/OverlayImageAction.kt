package com.purpletear.ai_conversation.ui.screens.home.components.overlay_image_action

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.button.ButtonComposable
import com.purpletear.ai_conversation.ui.component.button.ButtonTheme
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme
import com.purpletear.ai_conversation.ui.theme.LoadingColor

@Composable
@Preview(name = "QuotedTextComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_overlay_image_action),
                contentDescription = null,
            )
            OverlayImageAction(
                modifier = Modifier
                    .fillMaxWidth(0.88f),
                label = "Demo",
                url = "https://data.sutoko.app/resources/sutoko-ai/image/overlay-semibot-woman.jpg"
            )
        }
    }
}

@Composable
fun OverlayImageAction(modifier: Modifier = Modifier, label: String, url: String) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clipToBounds()
            .then(modifier)
            .clip(MaterialTheme.shapes.small)
            .background(LoadingColor)
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize()
                .clip(MaterialTheme.shapes.small),
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        ButtonComposable(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            theme = ButtonTheme.WhitePillArrow(),
            title = label
        )
    }
}