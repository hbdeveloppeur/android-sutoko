package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.CrimsonTextFontFamily
import com.purpletear.game.presentation.game_play.mapper.ITEMS_HORIZONTAL_PADDING

@Preview(name = "GameMessageText")
@Composable
private fun Preview() {
    Column {
        Box(
            Modifier
                .padding(4.dp)
        ) {
            MessageNarration(text = "C'est à ce moment qu'on entend quelque chose d'étrange")
        }
    }
}

@Composable
internal fun MessageNarration(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .padding(horizontal = ITEMS_HORIZONTAL_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 8.dp),
            text = text,
            color = Color.White,
            fontFamily = CrimsonTextFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

