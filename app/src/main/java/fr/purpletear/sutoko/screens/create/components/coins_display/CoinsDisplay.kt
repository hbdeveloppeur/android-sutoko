package fr.purpletear.sutoko.screens.create.components.coins_display

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import fr.purpletear.sutoko.shop.presentation.R as ShopR

private val CoinGold = Color(0xFFFCAC12)
private val DiamondBlue = Color(0xFF4DB9EC)
private val BackgroundDark = Color(0xFF4F442E)
private val BackgroundDiamond = Color(0xFF2E3A4F)

@Composable
internal fun CoinsDisplay(
    amount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int = ShopR.drawable.sutoko_item_coin,
    borderColor: Color = CoinGold,
    backgroundColor: Color = BackgroundDark,
    animated: Boolean = true
) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(end = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        AnimatedCoinAmount(amount = amount)
    }
}

@Composable
private fun AnimatedCoinAmount(amount: Int) {
    val animatedValue by animateIntAsState(
        targetValue = amount,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "coin_amount"
    )

    AnimatedContent(
        targetState = animatedValue,
        transitionSpec = {
            val isIncreasing = targetState > initialState
            slideInVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) { height -> if (isIncreasing) height else -height } + 
            fadeIn(animationSpec = tween(150)) togetherWith
            slideOutVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) { height -> if (isIncreasing) -height else height } + 
            fadeOut(animationSpec = tween(150))
        },
        modifier = Modifier.animateContentSize(),
        label = "coin_content"
    ) { value ->
        Text(
            text = value.toString(),
            color = Color.White,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
