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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
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
    val infiniteTransition = rememberInfiniteTransition(label = "shapes")

    // Shape 1: gentle float + slow rotation
    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rot1"
    )

    // Shape 2: different float + rotation
    val float2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = -45f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rot2"
    )

    // Shape 3: subtle scale + rotation
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale3"
    )
    val rotation3 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rot3"
    )

    // Shape 4: float + scale combo
    val float4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float4"
    )
    val rotation4 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rot4"
    )

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
        // Shape 1: Bottom left - gentle float + rotation
        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 22.dp, y = 26.dp + float1.dp)
                .size(48.dp)
                .rotate(rotation1)
                .alpha(0.6f)
        )

        // Shape 2: Bottom left upper - float + rotation
        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp, y = (-40).dp + float2.dp)
                .size(48.dp)
                .rotate(rotation2)
                .alpha(0.6f)
        )

        // Shape 3: Top right - scale + rotation
        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = (-24).dp)
                .size((42 * scale3).dp)
                .rotate(rotation3)
                .alpha(0.6f)
        )

        // Shape 4: Bottom right - float + rotation
        Image(
            painter = painterResource(id = R.drawable.rounded_square_outline_shape),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = 30.dp + float4.dp)
                .size(42.dp)
                .rotate(rotation4)
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
