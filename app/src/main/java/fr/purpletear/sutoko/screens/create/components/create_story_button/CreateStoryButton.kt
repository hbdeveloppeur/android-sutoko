package fr.purpletear.sutoko.screens.create.components.create_story_button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import fr.purpletear.sutoko.R

sealed class CreateStoryButtonVariant {
    abstract val backgroundColor: Color
    abstract val shapeColor: Color
    abstract val textColor: Color
    abstract val gradient: Brush?

    data object White : CreateStoryButtonVariant() {
        override val backgroundColor: Color = Color(0xFFDBDBDB)
        override val shapeColor: Color = Color(0xFF292929)
        override val textColor: Color = Color(0xFF292929)
        override val gradient: Brush? = null
    }

    data object Violet : CreateStoryButtonVariant() {
        override val backgroundColor: Color = Color(0xFF6827A4)
        override val shapeColor: Color = Color.White
        override val textColor: Color = Color.White
        override val gradient: Brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFA41CFF),
                Color(0xFFF01CFF)
            )
        )
    }
}

@Composable
internal fun CreateStoryButton(
    modifier: Modifier = Modifier,
    text: String,
    hint: String? = null,
    variant: CreateStoryButtonVariant = CreateStoryButtonVariant.Violet,
    onClick: () -> Unit
) {
    val gradient = variant.gradient
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (gradient != null) {
                    Modifier.background(gradient)
                } else {
                    Modifier.background(variant.backgroundColor)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        DecorativeShapes(color = variant.shapeColor)

        if (hint != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = text,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = variant.textColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = hint,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = variant.textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = text,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = variant.textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DecorativeShapes(color: Color) {
    val shapeModifier = Modifier
        .size(48.dp)
        .alpha(0.15f)

    Box(modifier = Modifier.fillMaxWidth()) {
        // Top-left shape
        Icon(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            tint = color,
            modifier = shapeModifier
                .align(Alignment.TopStart)
                .rotate(-15f)
        )

        // Top-right shape
        Icon(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            tint = color,
            modifier = shapeModifier
                .align(Alignment.TopEnd)
                .rotate(15f)
        )

        // Bottom-left shape
        Icon(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            tint = color,
            modifier = shapeModifier
                .align(Alignment.BottomStart)
                .rotate(15f)
        )

        // Bottom-right shape
        Icon(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            tint = color,
            modifier = shapeModifier
                .align(Alignment.BottomEnd)
                .rotate(-15f)
        )
    }
}
