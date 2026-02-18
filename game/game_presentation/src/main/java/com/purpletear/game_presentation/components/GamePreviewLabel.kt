package com.purpletear.game_presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.game_presentation.modifiers.gradientBorder
import com.purpletear.game_presentation.sealed.Background
import com.purpletear.game_presentation.sealed.toBrush
import com.example.sharedelements.R as SharedElementsR

@Composable
internal fun GamePreviewLabel(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.White,
    borderColor: Background = Background.Solid(Color.White.copy(alpha = 0.6f))
) {
    Box(
        modifier = modifier
            .widthIn(min = 40.dp)
            .clip(
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                brush = Background.Solid(Color.White.copy(alpha = 0.1f)).toBrush()
            )
            .gradientBorder(
                brush = borderColor.toBrush(),
                width = 1.dp,
                cornerRadius = 4.dp
            )
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .padding(bottom = 1.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily(
                Font(
                    resId = SharedElementsR.font.plus_jakarta_sans,
                    weight = FontWeight.Bold,
                )
            ),
            fontSize = 10.sp,
            color = textColor
        )
    }
}
