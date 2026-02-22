package com.example.sutokosharedelements.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.sharedelements.enums.DesignSystemTextSize
import com.example.sharedelements.enums.FontEnum

@Composable
fun SText(
    modifier : Modifier = Modifier,
    text: String,
    size: DesignSystemTextSize = DesignSystemTextSize.Normal,
    font: FontEnum,
    style: TextStyle = MaterialTheme.typography.body1,
    letterSpacing: TextUnit = 0.5.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    color: Color = Color.White,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier,
        textAlign = textAlign,
        text = text, fontSize = size.size, color = color, style = style.copy(
            letterSpacing = letterSpacing,
            lineHeight = lineHeight,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ), fontFamily = FontFamily(
            Font(
                font.font
            )
        )
    )
}


