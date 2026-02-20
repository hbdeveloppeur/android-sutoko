package fr.purpletear.sutoko.screens.create.components.create_story_button

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import fr.purpletear.sutoko.R

sealed class CreateStoryButtonVariant(
    val backgroundColor: Color,
    val shapeColor: Color,
    val textColor: Color,
    val gradient: Brush? = null
) {
    data object White : CreateStoryButtonVariant(
        backgroundColor = Color(0xFFDBDBDB),
        shapeColor = Color(0xFF292929),
        textColor = Color(0xFF292929)
    )

    data object Violet : CreateStoryButtonVariant(
        backgroundColor = Color(0xFF6827A4),
        shapeColor = Color.White,
        textColor = Color.White,
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFA41CFF), Color(0xFFF01CFF))
        )
    )
}

private data class ShapeConfig(
    val size: Dp,
    val floatRange: ClosedFloatingPointRange<Float> = 0f..0f,
    val floatDuration: Int = 3000,
    val rotationRange: ClosedFloatingPointRange<Float> = 0f..0f,
    val rotationDuration: Int = 4000,
    val scaleRange: ClosedFloatingPointRange<Float> = 1f..1f,
    val scaleDuration: Int = 2500
)

private val shapeConfigs = listOf(
    // Shape 1: Bottom left - gentle float + rotation
    Triple(Alignment.BottomStart, 22.dp, 26.dp) to
        ShapeConfig(size = 48.dp, floatRange = 0f..6f, rotationRange = -15f..-5f),
    // Shape 2: Bottom left upper - float + rotation
    Triple(Alignment.BottomStart, (-4).dp, (-40).dp) to
        ShapeConfig(
            size = 48.dp,
            floatRange = 0f..-5f,
            floatDuration = 3500,
            rotationRange = -45f..-35f,
            rotationDuration = 5000
        ),
    // Shape 3: Top right - scale + rotation
    Triple(Alignment.TopEnd, (-20).dp, (-24).dp) to
        ShapeConfig(
            size = 42.dp,
            scaleRange = 1f..1.08f,
            rotationRange = -30f..-20f,
            rotationDuration = 4500
        ),
    // Shape 4: Bottom right - float + rotation
    Triple(Alignment.BottomEnd, (-20).dp, 30.dp) to
        ShapeConfig(
            size = 42.dp,
            floatRange = 0f..4f,
            floatDuration = 2800,
            rotationRange = -30f..-40f,
            rotationDuration = 4200
        )
)

@Composable
internal fun CreateStoryButton(
    modifier: Modifier = Modifier,
    text: String,
    hint: String? = null,
    variant: CreateStoryButtonVariant = CreateStoryButtonVariant.Violet,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .let { mod ->
                variant.gradient?.let { mod.background(it) } ?: mod.background(variant.backgroundColor)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        DecorativeShapes(variant.shapeColor)
        ButtonContent(text, hint, variant.textColor)
    }
}

@Composable
private fun DecorativeShapes(shapeColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "shapes")

    Box {
        shapeConfigs.forEach { (position, config) ->
            AnimatedShape(
                position = position,
                config = config,
                shapeColor = shapeColor,
                transition = infiniteTransition
            )
        }
    }
}

@Composable
private fun BoxScope.AnimatedShape(
    position: Triple<Alignment, Dp, Dp>,
    config: ShapeConfig,
    shapeColor: Color,
    transition: InfiniteTransition
) {
    val (alignment, offsetX, offsetY) = position

    val float by transition.animateFloat(
        initialValue = config.floatRange.start,
        targetValue = config.floatRange.endInclusive,
        animationSpec = infiniteRepeatable(
            animation = tween(config.floatDuration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val rotation by transition.animateFloat(
        initialValue = config.rotationRange.start,
        targetValue = config.rotationRange.endInclusive,
        animationSpec = infiniteRepeatable(
            animation = tween(config.rotationDuration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val scale by transition.animateFloat(
        initialValue = config.scaleRange.start,
        targetValue = config.scaleRange.endInclusive,
        animationSpec = infiniteRepeatable(
            animation = tween(config.scaleDuration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Image(
        painter = painterResource(R.drawable.rounded_square_outline_shape),
        contentDescription = null,
        colorFilter = ColorFilter.tint(shapeColor),
        modifier = Modifier
            .align(alignment)
            .offset(x = offsetX, y = offsetY + float.dp)
            .size(config.size * scale)
            .rotate(rotation)
            .alpha(0.6f)
    )
}

@Composable
private fun ButtonContent(text: String, hint: String?, textColor: Color) {
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
            color = textColor,
            textAlign = TextAlign.Center
        )
        hint?.let {
            Text(
                text = it,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
