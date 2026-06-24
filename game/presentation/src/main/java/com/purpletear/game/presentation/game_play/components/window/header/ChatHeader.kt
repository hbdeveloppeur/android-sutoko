package com.purpletear.game.presentation.game_play.components.window.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sharedelements.theme.MontserratFontFamily
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.Avatar

@Preview
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .padding(2.dp)
            .height(52.dp)
            .aspectRatio(291f / 80f),
        drawable = R.drawable.ppreview_sms_game_header,
    ) {
        ChatHeader()
    }
}

@Composable
internal fun ChatHeader(
    modifier: Modifier = Modifier,
    characterName: String = stringResource(R.string.chat_header_character_name),
    status: String = stringResource(R.string.chat_header_status_away),
) {
    Row(
        Modifier.then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        BackButton()
        Avatar(size = 44.dp, imageModel = R.drawable.tmp_avatar)
        Info(characterName = characterName, status = status)
    }
}

@Composable
internal fun Info(
    characterName: String,
    status: String,
) {
    Column(
        Modifier
            .padding(start = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Label(text = characterName)
        Row(
            Modifier.padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(12.dp)
                    .alpha(0.4f),
                model = R.drawable.ic_moon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            SubLabel(text = status)
        }
    }
}

@Composable
internal fun Label(text: String) {
    val upperText = remember(text) { text.uppercase() }
    Text(
        text = upperText,
        color = Color.White,
        fontSize = 13.sp,
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
internal fun SubLabel(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        fontSize = 12.sp,
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .size(34.dp)
            .aspectRatio(1f)
            .then(modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back_button),
            contentDescription = stringResource(R.string.chat_header_back_button_description),
            modifier = Modifier
                .size(14.dp)
                .graphicsLayer {
                    translationX = -6f
                },
            tint = Color.White.copy(alpha = 0.5f),
        )
    }
}
