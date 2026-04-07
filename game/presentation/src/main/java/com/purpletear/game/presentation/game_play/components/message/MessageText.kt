package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.MontserratFontFamily
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.game.debug.R as DebugR

@Preview(name = "GameMessageText")
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .height(52.dp)
            .aspectRatio(564f / 166f),
        drawable = DebugR.drawable.preview_messagedest,
    ) {
        Column {
            Box(
                Modifier
                    .padding(4.dp)
            ) {
                MessageText(text = "Je ne crois pas")
            }
            Box(
                Modifier
                    .padding(4.dp)
            ) {
                MessageText(text = "Je ne crois pas que tu vas respecter ce que tu viens de dire")
            }
        }
    }
}

@Composable
internal fun MessageText(
    text: String,
    modifier: Modifier = Modifier
) {
    MessageBubble(modifier = modifier) {
        Avatar(
            modifier = Modifier.background(Color.Blue),
            size = 26.dp,
            borderWidth = 1.4.dp,
            drawable = R.drawable.tmp_avatar
        )

        Text(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .padding(end = 24.dp),
            text = text,
            color = Color.White,
            fontFamily = MontserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.7.sp,
        )
    }
}

