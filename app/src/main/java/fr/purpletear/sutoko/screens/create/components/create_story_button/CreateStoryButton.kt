package fr.purpletear.sutoko.screens.create.components.create_story_button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotated shape behind text, half out on bottom left

        // Rotated shape behind text, half out on bottom left (outside clip)
        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 22.dp, y = 26.dp)
                .size(48.dp)
                .rotate(-15f)
                .alpha(0.6f)
        )

        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp, y = (-40).dp)
                .size(48.dp)
                .rotate(-45f)
                .alpha(0.6f)
        )

        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = (-24).dp)
                .size(42.dp)
                .rotate(-30f)
                .alpha(0.6f)
        )

        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = 30.dp)
                .size(42.dp)
                .rotate(-30f)
                .alpha(0.6f)
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
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
            if (hint != null) {
                Text(
                    text = hint,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = variant.textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
