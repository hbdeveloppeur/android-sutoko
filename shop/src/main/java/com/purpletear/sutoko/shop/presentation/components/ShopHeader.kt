package com.purpletear.sutoko.shop.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import com.purpletear.sutoko.shop.R
import com.purpletear.sutoko.shop.presentation.ShopHeaderState

private const val BALANCE_UNAVAILABLE_PLACEHOLDER = "-"

/**
 * Back button, screen title and subtitle.
 */
@Composable
fun ShopTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(0.7f),
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.shop_sutoko_shop_title).uppercase(),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color(0xFFFEFFFF),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.shop_sutoko_shop_subtitle),
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFFF4F4F4),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 3.dp)
                    .widthIn(max = 300.dp),
            )
        }
    }
}

/**
 * Balance area below the title, driven by [ShopHeaderState]:
 * sign-in button when disconnected, retryable "-" placeholders on failure,
 * live coins/diamonds otherwise.
 */
@Composable
fun ShopHeader(
    state: ShopHeaderState,
    onSignInClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            ShopHeaderState.Disconnected -> SignInButton(onClick = onSignInClick)

            ShopHeaderState.Failed -> BalancePill(
                coins = BALANCE_UNAVAILABLE_PLACEHOLDER,
                diamonds = BALANCE_UNAVAILABLE_PLACEHOLDER,
                onClick = onRetryClick,
            )

            ShopHeaderState.Loading -> BalancePill(
                coins = BALANCE_UNAVAILABLE_PLACEHOLDER,
                diamonds = BALANCE_UNAVAILABLE_PLACEHOLDER,
            )

            is ShopHeaderState.Loaded -> BalancePill(
                coins = state.balance.coins.toString(),
                diamonds = state.balance.diamonds.toString(),
            )
        }
    }
}

@Composable
private fun SignInButton(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.shop_sutoko_sign_in),
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.48.sp,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFF447C))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/**
 * Rounded dark pill showing the coins and diamonds amounts.
 */
@Composable
fun BalancePill(
    coins: String,
    diamonds: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    iconSize: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color(0x44000000))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 18.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedAmountText(
            value = coins,
            fontSize = fontSize,
        )
        Image(
            painter = painterResource(R.drawable.shop_sutoko_item_coin),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 3.dp)
                .size(iconSize)
                .rotate(45f),
        )
        Spacer(modifier = Modifier.width(6.dp))
        AnimatedAmountText(
            value = diamonds,
            fontSize = fontSize,
        )
        Image(
            painter = painterResource(R.drawable.shop_sutoko_ic_diamond),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 3.dp)
                .size(iconSize)
                .rotate(45f),
        )
    }
}
