package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.MontserratFontFamily
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R

@Preview
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .padding(2.dp)
            .aspectRatio(718f / 112f),
        drawable = R.drawable.game_presentation_preview_message_event,
    ) {
        MessageEvent(
            title = "Message de Aïko",
            subtitle = "\"C'est à ce moment qu'on entend quelque chose d'étrange\""
        )
    }
}

@Composable
internal fun MessageEvent(title: String, subtitle: String) {
    Row(Modifier.padding(start = 24.dp)) {
        Box(
            Modifier
                .height(50.dp)
                .padding(vertical = 6.dp)
                .width(1.dp)
                .background(Color.White)
        )
        Column(
            Modifier
                .widthIn(max = 250.dp)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = MontserratFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            )
            Text(
                text = subtitle,
                color = Color.LightGray,
                fontFamily = MontserratFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
    }
}