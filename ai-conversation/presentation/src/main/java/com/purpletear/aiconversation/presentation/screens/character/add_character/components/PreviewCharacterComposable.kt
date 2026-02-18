package com.purpletear.aiconversation.presentation.screens.character.add_character.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "PreviewCharacterComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_character),
                contentDescription = null,
            )
            PreviewCharacterComposable(
                Modifier.fillMaxWidth(0.88f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PreviewCharacterComposable(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Avatar("https://data.sutoko.app/resources/sutoko-ai/image/avatar_hanna.jpg")
            Text(
                text = "Anna Belle",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Separator()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            )
            {
                Text(
                    "Online",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                OnlineCircle()
            }
        }
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = "Issue d’une école de sorciers, Anna est une fille simple, agréable, elle aime lire et jouer du violon.",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = Color.White
        )

        FlowRow(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Badge(text = "Funny")
            Badge(text = "Badass")
            Badge(text = "Courageous")
        }
    }
}

@Composable
private fun Avatar(url: String) {
    AsyncImage(
        modifier = Modifier
            .size(26.dp)
            .border(1.dp, Color.White, CircleShape)
            .clip(CircleShape),
        model =
        ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun Separator() {
    Box(
        Modifier
            .background(Color.White.copy(0.7f))
            .height(16.dp)
            .width(1.dp)
    )
}

@Composable
private fun OnlineCircle() {
    Box(
        modifier = Modifier
            .height(4.dp)
            .width(4.dp)
            .clip(CircleShape)
            .background(Color(0xFF17ACDA))
    )
}

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFFFF31AC).copy(0.4f), CircleShape)
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clip(CircleShape)
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}