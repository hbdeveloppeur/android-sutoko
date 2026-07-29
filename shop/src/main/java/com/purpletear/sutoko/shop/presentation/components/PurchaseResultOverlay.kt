package com.purpletear.sutoko.shop.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.sharedelements.components.AnimatedGradientBorderBox
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import com.purpletear.sutoko.shop.R
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.presentation.PurchaseOverlayState
import com.purpletear.sutoko.shop.presentation.PurchasePhase
import com.example.sharedelements.R as SharedElementsR

/**
 * Full-screen purchase result overlay.
 *
 * - [PurchasePhase.CREDITING]: payment confirmed by Play, backend credit in progress.
 *   Shown instantly at sheet close so the user never wonders if they paid for nothing.
 * - [PurchasePhase.SUCCESS]: purchase verified; celebrates the gain (+N coins, haptic).
 * - [PurchasePhase.PENDING]: payment pending validation (e.g. cash methods).
 */
@Composable
fun PurchaseResultOverlay(
    state: PurchaseOverlayState?,
    balance: Balance,
    isUserConnected: Boolean,
    onContinue: () -> Unit,
) {
    AnimatedVisibility(
        visible = state != null,
        enter = fadeIn(animationSpec = tween(380)),
        exit = fadeOut(animationSpec = tween(200)),
    ) {
        state ?: return@AnimatedVisibility

        val haptic = LocalHapticFeedback.current
        val context = LocalContext.current
        LaunchedEffect(state.phase) {
            if (state.phase == PurchasePhase.SUCCESS) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                playCoinSound(context)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Image(
                painter = painterResource(SharedElementsR.drawable.shared_elements_sutoko_account_creation_header_background),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .aspectRatio(319f / 172f),
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(
                        when (state.phase) {
                            PurchasePhase.CREDITING -> R.string.shop_sutoko_payment_received_title
                            PurchasePhase.PENDING -> R.string.shop_sutoko_shop_unlock_item_pending_title
                            PurchasePhase.SUCCESS -> R.string.shop_sutoko_congrats
                        }
                    ),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFFD8D8D8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
                Text(
                    text = stringResource(
                        when (state.phase) {
                            PurchasePhase.CREDITING -> R.string.shop_sutoko_payment_received_subtitle
                            PurchasePhase.PENDING -> R.string.shop_sutoko_shop_unlock_item_pending_title_subtitle
                            PurchasePhase.SUCCESS ->
                                if (state.pack.sku.contains("premium")) R.string.shop_sutoko_shop_unlock_item_title_premium
                                else R.string.shop_sutoko_shop_unlock_item_title_pack
                        }
                    ),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFFFAFAFA),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 300.dp),
                )

                val packImageRes = when (state.pack.type) {
                    CoinsPackType.Low -> R.drawable.shop_sutoko_shop_item_bag
                    CoinsPackType.Medium -> R.drawable.shop_sutoko_shop_item_chest
                    CoinsPackType.High -> R.drawable.shop_sutoko_shop_item_chest2
                }
                val imageModifier = Modifier
                    .padding(top = 12.dp)
                    .size(200.dp)
                if (state.phase == PurchasePhase.CREDITING) {
                    Image(
                        painter = painterResource(packImageRes),
                        contentDescription = null,
                        modifier = imageModifier,
                    )
                } else {
                    ZoomInImage(imageRes = packImageRes, modifier = imageModifier)
                }

                when (state.phase) {
                    PurchasePhase.CREDITING -> {
                        CircularProgressIndicator(
                            color = Color(0xFFDF3288),
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(32.dp),
                        )
                        CoinsGainedText(
                            coins = state.pack.coins,
                            muted = true,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    PurchasePhase.SUCCESS -> {
                        BouncingCoinsGainedText(
                            coins = state.pack.coins,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        if (balance.isLoaded()) {
                            BalancePill(
                                coins = balance.coins.toString(),
                                diamonds = balance.diamonds.toString(),
                                fontSize = 18.sp,
                                iconSize = 23.dp,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }

                    PurchasePhase.PENDING -> Unit
                }

                val warningRes = when (state.phase) {
                    PurchasePhase.PENDING -> R.string.shop_sutoko_shop_unlock_item_pending_title_description
                    PurchasePhase.SUCCESS ->
                        if (!isUserConnected) R.string.shop_sutoko_shop_unlock_item_connection_warning
                        else null
                    PurchasePhase.CREDITING -> null
                }
                warningRes?.let {
                    Text(
                        text = stringResource(it),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFFD8D8D8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .widthIn(max = 300.dp),
                    )
                }
            }

            if (state.phase == PurchasePhase.SUCCESS) {
                AnimatedGradientBorderBox(modifier = Modifier.fillMaxSize())
            }

            if (state.phase != PurchasePhase.CREDITING) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp)
                        .size(width = 210.dp, height = 50.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFDF3288))
                        .clickable(onClick = onContinue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.shop_sutoko_continue),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * "+N coins" amount with a coin icon; muted while the credit is still in flight.
 */
@Composable
private fun CoinsGainedText(
    coins: Int,
    muted: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.shop_sutoko_coins_gained, coins),
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = if (muted) Color(0xFF9E9E9E) else Color(0xFFFFC94D),
        )
        Image(
            painter = painterResource(R.drawable.shop_sutoko_item_coin),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(22.dp)
                .rotate(45f),
        )
    }
}

@Composable
private fun BouncingCoinsGainedText(
    coins: Int,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.5f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) + fadeIn(),
        modifier = modifier,
    ) {
        CoinsGainedText(coins = coins, muted = false)
    }
}

@Composable
private fun ZoomInImage(
    imageRes: Int,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.3f, animationSpec = tween(580)) + fadeIn(),
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
