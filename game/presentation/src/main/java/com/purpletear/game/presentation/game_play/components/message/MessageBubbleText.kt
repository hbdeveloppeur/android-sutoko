package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.WorkSansFontFamily
import kotlin.math.ceil

@Composable
internal fun MessageBubbleText(text: String, color: Color) {
    val style = LocalTextStyle.current.copy(
        color = color,
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val textMeasurer = rememberTextMeasurer(cacheSize = 1)

    Text(
        text = text,
        style = style,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .layout { measurable, constraints ->
                val textLayout = textMeasurer.measure(
                    text = text,
                    style = style,
                    constraints = constraints.copy(minWidth = 0, minHeight = 0),
                    layoutDirection = layoutDirection,
                    density = this,
                )
                // Round up to preserve word boundaries; subtract left for RTL paragraphs.
                val lineWidth = (0 until textLayout.lineCount).maxOfOrNull { line ->
                    textLayout.getLineRight(line) - textLayout.getLineLeft(line)
                } ?: 0f
                val width = constraints.constrainWidth(ceil(lineWidth).toInt())
                val placeable = measurable.measure(
                    constraints.copy(minWidth = width, maxWidth = width)
                )
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            },
    )
}
