package com.purpletear.sutoko.shop.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sharedelements.theme.Poppins
import com.purpletear.sutoko.shop.R
import com.purpletear.sutoko.shop.domain.model.PackItem

/**
 * Large "Mega Pack" card with a looping light effect behind the chest.
 * Disabled until a price is known.
 */
@Composable
fun LargeCoinsPackCard(
    packItem: PackItem?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(30.dp)
    val enabled = !packItem?.formattedPrice.isNullOrBlank()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.shop_sutoko_effect_light)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.shop_sutoko_shop_item_mega_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 0.7f,
                    modifier = Modifier
                        .size(190.dp)
                        .alpha(0.6f),
                )
                Image(
                    painter = painterResource(R.drawable.shop_sutoko_shop_item_chest2),
                    contentDescription = null,
                    modifier = Modifier
                        .sizeIn(maxHeight = 120.dp)
                        .scale(scaleX = -1f, scaleY = 1f)
                        .graphicsLayer {
                            scaleX = 2f
                            scaleY = 2f
                        },
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(top = 19.dp, end = 16.dp, bottom = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.shop_sutoko_shop_pack_name_mega),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                )
                PackAmountsRow(
                    coins = packItem?.pack?.coins,
                    diamonds = packItem?.pack?.diamonds,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = stringResource(R.string.shop_sutoko_shop_best_pack_label),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 22.dp,
                                bottomEnd = 22.dp,
                                bottomStart = 22.dp,
                            )
                        )
                        .background(Color(0x2CFFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    BuyCta(
                        price = packItem?.formattedPrice.orEmpty(),
                        enabled = enabled,
                        onClick = onClick,
                    )
                }
            }
        }
    }
}
