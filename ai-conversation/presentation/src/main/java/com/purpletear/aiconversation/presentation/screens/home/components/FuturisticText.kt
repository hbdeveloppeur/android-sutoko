package com.purpletear.aiconversation.presentation.screens.home.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.purpletear.aiconversation.presentation.R

@Composable
internal fun FuturisticText(modifier : Modifier = Modifier, fontSize : TextUnit, text: String) {
    val fontFamily = FontFamily(
        Font(R.font.orbitron, FontWeight.Normal)
    )

    Text(modifier = modifier, text = text, color = Color.White, fontFamily = fontFamily, fontSize = fontSize)
}