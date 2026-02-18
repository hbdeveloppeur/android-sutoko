package com.purpletear.ai_conversation.ui.component.buy_tokens_dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.events.BuyTokensDialogAbort
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states.BuyTokensCoinsDialogState
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states.BuyTokensDialogState
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.viewModels.BuyTokensDialogViewModel

@Composable
fun BuyTokensDialogComposable(
    modifier: Modifier = Modifier,
    onClickLogin: () -> Unit,
    viewModel: BuyTokensDialogViewModel = hiltViewModel(),
) {
    BuyDialogContainer(modifier) {
        BuyDialogColumn(
            modifier = Modifier.animateContentSize(
                tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        ) {
            BuyDialogRow {
                Title(text = stringResource(id = viewModel.titleState.value.title))
                CoinIcon()
            }
            viewModel.titleState.value.message?.let {
                BuyDialogRow {
                    SubTitle(text = stringResource(id = it))
                }
            }
            BuyDialogRow(
                Modifier
                    .padding(top = 4.dp)
            ) {
                when (val coinsState = viewModel.coinsState.value) {
                    is BuyTokensCoinsDialogState.Loading -> {
                        CoinAmount(
                            amount = coinsState.messages,
                            icon = R.drawable.ai_conversation_presentation_coin_message
                        )
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }

                    is BuyTokensCoinsDialogState.Loaded -> {
                        CoinAmount(
                            amount = coinsState.messages,
                            icon = R.drawable.ai_conversation_presentation_coin_message
                        )
                    }

                    BuyTokensCoinsDialogState.NotLoggedIn -> {

                    }
                }
            }

            Box(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                when (val state = viewModel.state.value) {
                    is BuyTokensDialogState.Loading -> {
                        LoadingComposable()
                    }

                    BuyTokensDialogState.Login -> {
                        LoginComposable(
                            onClick = onClickLogin
                        )
                    }

                    is BuyTokensDialogState.Packs -> {
                        BuyDialogColumn(Modifier.padding(horizontal = 12.dp)) {
                            viewModel.messagesPacks.value.forEachIndexed { index, aiMessagePack ->
                                val isFirst = index == 0
                                val drawableId = listOf(
                                    R.drawable.ai_conversation_presentation_button_buy_token_low_pack,
                                    R.drawable.ai_conversation_presentation_button_buy_token_medium_pack,
                                    R.drawable.ai_conversation_presentation_button_buy_token_high_pack,
                                    R.drawable.ai_conversation_presentation_button_buy_token_supra_pack,
                                )[index.coerceAtMost(3)]
                                Button(
                                    title = stringResource(
                                        R.string.ai_conversation_cta_get_coins,
                                        aiMessagePack.tokensCount
                                    ),
                                    subtitle = "${aiMessagePack.productDetails?.price}",
                                    modifier = Modifier.padding(top = if (isFirst) 16.dp else 8.dp),
                                    background = drawableId,
                                    onClick = {
                                        viewModel.onClickMessagePack(
                                            aiMessagePack
                                        )
                                    }
                                )
                            }
                        }
                    }

                    is BuyTokensDialogState.Error -> {
                        val error = state.message
                        ErrorComposable(error.asString())
                    }

                    is BuyTokensDialogState.Error.NotEnoughCoins -> {
                        val error = state.message
                        NotEnoughCoinsErrorComposable(error.asString()) {
                            viewModel.displayListOfPacks()
                        }
                    }

                    is BuyTokensDialogState.Success -> {
                        val error = state.message
                        SuccessComposable(error.asString())
                    }

                    is BuyTokensDialogState.Confirm.Buy -> {
                        val pack = state.pack
                        BuyDialogColumn {
                            SubTitle(
                                text = stringResource(
                                    id = R.string.ai_conversation_presentation_message_confirm_buy_pack,
                                    pack.tokensCount,
                                )
                            )
                            BuyDialogRow(modifier = Modifier.padding(top = 16.dp)) {
                                CallToActionButtonComposable(
                                    title = stringResource(R.string.ai_conversation_cancel),
                                    subtitle = null,
                                    onClick = {
                                        viewModel.cancelAction(BuyTokensDialogAbort.Buy)
                                    },
                                )
                                CallToActionButtonComposable(
                                    title = stringResource(R.string.ai_conversation_buy),
                                    subtitle = "${pack.tokensCount} messages",
                                    backgroundColor = Color(0xFFFF087F),
                                    onClick = {

                                    },
                                )
                            }
                        }
                    }

                    BuyTokensDialogState.Confirm.Try -> {
                        BuyDialogRow {
                            CallToActionButtonComposable(
                                title = stringResource(R.string.ai_conversation_cancel),
                                subtitle = null,
                                onClick = {
                                    viewModel.cancelAction(BuyTokensDialogAbort.Try)
                                },
                            )

                            CallToActionButtonComposable(
                                title = stringResource(R.string.ai_conversation_start),
                                subtitle = stringResource(R.string.ai_conversation_count_try),
                                backgroundColor = Color(0xFFFF087F),
                                onClick = {
                                    viewModel.onTryClicked()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingComposable() {
    Column(
        modifier = Modifier
            .heightIn(min = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(20.dp)
                .padding(bottom = 8.dp),
            color = Color.LightGray,
            strokeWidth = 2.dp
        )
        SubTitle(text = stringResource(id = R.string.ai_conversation_loading))
    }
}

@Composable
private fun ResultLottieAnimation(size: Dp, isSuccessful: Boolean) {
    val rawRes =
        if (isSuccessful) R.raw.ai_conversation_presentation_animation_checked else R.raw.ai_conversation_presentation_animation_error
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    val progress by animateLottieCompositionAsState(
        composition
    )
    LottieAnimation(
        modifier = Modifier
            .size(size),
        composition = composition,
        progress = { progress },
    )
}

@Composable
internal fun LoginComposable(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SubTitle(text = stringResource(id = R.string.ai_conversation_presentation_error_user_not_logged_in))
        PillButton(text = stringResource(id = R.string.ai_conversation_signin)) {
            onClick()
        }
    }
}

@Composable
internal fun PillButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable {
                onClick()
            }
            .background(Color(0xFFFF087F))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
}


@Composable
private fun ErrorComposable(message: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ResultLottieAnimation(
            size = 32.dp,
            isSuccessful = false
        )
        SubTitle(
            text = message
        )
    }
}

@Composable
private fun NotEnoughCoinsErrorComposable(message: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ResultLottieAnimation(
            size = 32.dp,
            isSuccessful = false
        )
        SubTitle(
            text = message
        )
        PillButton(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.ai_conversation_presentation_open_shop),
            onClick = onClick
        )
    }
}

@Composable
private fun SuccessComposable(message: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ResultLottieAnimation(size = 52.dp, isSuccessful = true)
        SubTitle(
            text = message
        )
    }
}


@Composable
private fun Button(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    background: Int,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .widthIn(min = 200.dp, max = 400.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            contentScale = ContentScale.Crop,
            painter = painterResource(id = background),
            contentDescription = null
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 14.sp
            )
            BuyDialogRow(spacing = 4.dp) {
                Text(
                    modifier = Modifier.alpha(0.8f),
                    text = subtitle,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
                CoinIcon(
                    modifier = Modifier.alpha(0.8f),
                    size = 12.dp,
                    id = R.drawable.ai_conversation_presentation_item_coin,
                )
            }
        }
        Image(
            modifier = Modifier
                .padding(end = 16.dp)
                .align(Alignment.CenterEnd)
                .size(18.dp),
            painter = painterResource(id = R.drawable.cart),
            contentDescription = null
        )
    }
}


@Composable
private fun BuyDialogRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

@Composable
private fun Title(text: String, fontSize: TextUnit = 18.sp) {
    Text(
        text = text, style = MaterialTheme.typography.titleLarge.copy(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ), color = Color.White, fontSize = fontSize
    )
}

@Composable
private fun SubTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(horizontal = 8.dp),
        lineHeight = 16.sp,
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ),
        fontSize = 12.sp,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CoinIcon(
    modifier: Modifier = Modifier,
    size: Dp = 19.dp,
    id: Int = R.drawable.ai_conversation_presentation_coin_message
) {
    Image(
        modifier = Modifier
            .size(size)
            .then(modifier),
        painter = painterResource(id = id),
        contentDescription = null
    )
}

@Composable
private fun BuyDialogColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun CoinAmount(modifier: Modifier = Modifier, amount: Int, icon: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF212121))
            .padding(
                horizontal = 10.dp,
            )
            .height(24.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = amount.toString(),
            color = Color.White,
            fontSize = 12.sp
        )
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
    }
}


@Composable
private fun BuyDialogContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF101010),
                        Color(0xFF090909),
                    )
                )
            )
            .widthIn(max = 400.dp)
            .fillMaxWidth()
    ) {
        BuyDialogDivider()
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .navigationBarsPadding()
                .padding(top = 8.dp)
                .align(Alignment.BottomCenter)
        ) {
            content()
        }
    }
}

@Composable
private fun BuyDialogDivider() {
    Box(
        modifier = Modifier
            .background(color = Color(0x11FFFFFF))
            .height(1.dp)
            .fillMaxWidth()
            .alpha(0.1f)
    )
}