package com.purpletear.game.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins

/**
 * Position of the optional icon relative to the text in [SimpleButton].
 * RTL-aware: [Start] is the leading side (left in LTR), [End] the trailing side.
 */
enum class SimpleButtonIconSide { Start, End }

/**
 * A simple button with text and an optional icon.
 * Used for primary actions in game screens.
 *
 * @param iconSide Side of the text on which the icon is drawn. Defaults to [SimpleButtonIconSide.End].
 */
@Composable
fun SimpleButton(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = 12.sp,
    horizontalPadding: Dp = 20.dp,
    verticalPadding: Dp = 12.dp,
    textColor: Color = Color.Black,
    backgroundColor: Color = Color(0xFFF8F8F8),
    imageVector: ImageVector? = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    iconSide: SimpleButtonIconSide = SimpleButtonIconSide.End,
    onClick: () -> Unit
) {
    val iconSize = with(LocalDensity.current) { fontSize.toDp() }
    val shape = RoundedCornerShape(7.dp)
    val icon: (@Composable () -> Unit)? = imageVector?.let { vector ->
        {
            Icon(
                imageVector = vector,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (iconSide == SimpleButtonIconSide.Start) icon?.invoke()
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Poppins
        )
        if (iconSide == SimpleButtonIconSide.End) icon?.invoke()
    }
}
