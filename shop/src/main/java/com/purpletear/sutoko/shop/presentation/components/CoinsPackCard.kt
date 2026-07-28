package com.purpletear.sutoko.shop.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import com.purpletear.sutoko.shop.R
import com.purpletear.sutoko.shop.domain.model.PackItem

/** Card gradients, ported from the legacy XML drawables (start color at the bottom). */
enum class PackGradient(val brush: Brush) {
    Purple(Brush.verticalGradient(listOf(Color(0xFF683081), Color(0xFF1B1827)))),
    Green(Brush.verticalGradient(listOf(Color(0xFF3AB3B0), Color(0xFF55367D)))),
}

internal val BuyCtaBrush =
    Brush.verticalGradient(listOf(Color(0xFFFD5392), Color(0xFFF86F64)))

/**
 * Small coins pack card (Starter / Treasure). Disabled until a price is known.
 */
@Composable
fun CoinsPackCard(
    packItem: PackItem?,
    @StringRes nameRes: Int,
    @DrawableRes imageRes: Int,
    gradient: PackGradient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showStars: Boolean = false,
) {
    val shape = RoundedCornerShape(26.dp)
    val enabled = !packItem?.formattedPrice.isNullOrBlank()

    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .aspectRatio(159f / 220f)
            .clip(shape)
            .background(gradient.brush)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(nameRes),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 14.dp),
            )
            PackAmountsRow(
                coins = packItem?.pack?.coins,
                diamonds = packItem?.pack?.diamonds,
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (showStars) {
                    Image(
                        painter = painterResource(R.drawable.shop_ic_stars_multi),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(68.dp)
                            .alpha(0.1f),
                    )
                }
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .sizeIn(maxWidth = 180.dp)
                        .graphicsLayer {
                            scaleX = 2f
                            scaleY = 2f
                        },
                )
            }
            BuyCta(
                price = packItem?.formattedPrice.orEmpty(),
                enabled = enabled,
                onClick = onClick,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
internal fun PackAmountsRow(
    coins: Int?,
    diamonds: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.shop_sutoko_item_coin),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = coins?.toString() ?: BALANCE_PLACEHOLDER,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp),
        )
        Image(
            painter = painterResource(R.drawable.shop_sutoko_ic_diamond),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(22.dp)
                .rotate(45f),
        )
        Text(
            text = diamonds?.toString() ?: BALANCE_PLACEHOLDER,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
internal fun BuyCta(
    price: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = price,
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(BuyCtaBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 6.dp),
    )
}

internal const val BALANCE_PLACEHOLDER = "-"
