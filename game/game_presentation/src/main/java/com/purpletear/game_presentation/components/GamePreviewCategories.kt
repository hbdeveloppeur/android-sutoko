package com.purpletear.game_presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.sutoko.game.model.Game

@Composable
internal fun GamePreviewCategories(modifier: Modifier = Modifier, game: Game) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = game.metadata.categories.joinToString(" • ") { category -> category.replaceFirstChar { it.uppercase() } },
            fontFamily = PlusJakartaSansFontFamily,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
