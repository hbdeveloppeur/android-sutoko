package com.purpletear.sutoko.popup_presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sharedelements.theme.SutokoTypography
import com.purpletear.sutoko.popup.presentation.R
import fr.purpletear.sutoko.popup.domain.EditTextPopUp
import fr.purpletear.sutoko.popup.domain.PopUpIconAnimation
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.PopUpIconUrl
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.RegularPopUp
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import kotlinx.coroutines.delay

@Composable
fun PopUpComposable(
    modifier: Modifier = Modifier,
) {

    val viewModel: PopUpViewModel = hiltViewModel()
    val isVisible = viewModel.getIsVisiblePopUpUseCase().collectAsState().value
    val popUp = viewModel.popUpRepository.popUp.collectAsState().value

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(0.6f))
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (popUp is RegularPopUp) {
                        return@clickable
                    }
                    viewModel.popUpRepository.interact(PopUpUserInteraction.Dismiss)
                }, contentAlignment = Alignment.Center
        ) {
            when (popUp) {
                is SutokoPopUp -> SutokoPopUpComposable(
                    modifier = modifier,
                    popUp = popUp,
                    onTapButton = {
                        viewModel.popUpRepository.interact(PopUpUserInteraction.Confirm)
                    }
                )

                is RegularPopUp -> RegularPopUpComposable(
                    modifier = modifier,
                    popUp = popUp,
                    onTapButton = { text ->
                        if (null == text) {
                            viewModel.popUpRepository.interact(PopUpUserInteraction.Confirm)
                            return@RegularPopUpComposable
                        }
                        viewModel.popUpRepository.interact(PopUpUserInteraction.ConfirmText(text))
                    },
                    onDismiss = {
                        viewModel.popUpRepository.interact(PopUpUserInteraction.Dismiss)
                    }
                )
            }
        }
    }
}


@Composable
private fun SutokoPopUpComposable(
    modifier: Modifier = Modifier,
    popUp: SutokoPopUp,
    onTapButton: (() -> Unit)? = null
) {
    Column(
        modifier =
            modifier
                .clip(shape = RoundedCornerShape(26.dp))
                .background(Color.White)
                .widthIn(0.dp, 240.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {

                },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {

            Box {
                Image(
                    modifier = Modifier.aspectRatio(1895 / 795f),
                    painter = painterResource(R.drawable.pop_up_header_violet_optimized),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(R.drawable.pop_up_shape),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .aspectRatio(100 / 13f)
                        .offset(y = 0.2.dp)
                )
            }
            val imageModifier = Modifier
                .align(Alignment.BottomCenter)
                .size(popUp.iconHeight ?: 64.dp)
                .offset(y = popUp.offsetY ?: 5.dp)
            when (popUp.icon) {
                is PopUpIconAnimation -> {
                    val iconAnimation = popUp.icon as PopUpIconAnimation
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(iconAnimation.id)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition,
                        iterations = if (iconAnimation.isLooping) LottieConstants.IterateForever else 1,
                        clipSpec = LottieClipSpec.Progress(
                            min = iconAnimation.startingFrame,
                            max = iconAnimation.endingFrame
                        )
                    )
                    LottieAnimation(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(popUp.iconHeight ?: 100.dp)
                            .offset(y = popUp.offsetY ?: 5.dp)
                            .graphicsLayer {
                                scaleX = 1.6f
                                scaleY = 1.6f
                            },
                        composition = composition,
                        progress = { progress },
                    )
                }

                is PopUpIconDrawable -> {
                    Image(
                        painter = painterResource((popUp.icon as PopUpIconDrawable).id),
                        contentDescription = null,
                        modifier = imageModifier,
                    )
                }

                is PopUpIconUrl -> {
                    AsyncImage(
                        modifier = imageModifier.clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data((popUp.icon as PopUpIconUrl).url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        Content(
            title = popUp.title?.asString() ?: "",
            description = popUp.description?.asString() ?: "",
            buttonText = popUp.buttonText?.asString() ?: "",
            onTapButton = onTapButton
        )
    }

}

@Composable
private fun RegularPopUpComposable(
    modifier: Modifier = Modifier,
    popUp: RegularPopUp,
    onDismiss: (() -> Unit)?,
    onTapButton: ((String?) -> Unit)?
) {
    val focusRequester = remember { FocusRequester() }
    val textInput: MutableState<String?> = remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        try {
            delay(280)
            focusRequester.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier
            .offset(y = (-20).dp)
            .width(250.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE3E6E8))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }
            ),
    ) {
        Text(
            text = popUp.title.asString(),
            style = MaterialTheme.typography.body2.copy(
                color = Color(0xFF202F3B),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .padding(top = 8.dp)
        )
        if (popUp is EditTextPopUp) {
            Column(
                Modifier
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                BasicTextField(
                    value = textInput.value ?: "",
                    cursorBrush = SolidColor(Color.Black.copy(0.7f)),
                    enabled = true,
                    onValueChange = { text ->
                        textInput.value = text
                    },
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .height(44.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFC4CCD5))
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (textInput.value?.isEmpty() == true) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = popUp.placeholder.asString(),
                                    style = MaterialTheme.typography.body2.copy(
                                        color = Color(0xFF818288),
                                        fontSize = 12.sp,
                                    ),
                                )
                            }
                        } else {
                            it()
                        }
                    }
                }

                Box(
                    Modifier
                        .height(2.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF569BE0))
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.popup_button_cancel),
                style = MaterialTheme.typography.button.copy(color = Color(0xFF706D5F)),
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        textInput.value = ""
                        onDismiss?.invoke()
                    }
            )
            Text(
                stringResource(R.string.popup_button_confirm),
                style = MaterialTheme.typography.button.copy(color = Color(0xFF08475B)),
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        onTapButton?.invoke(textInput.value)
                    }
            )
        }
    }
}

@Composable
private fun Content(
    title: String,
    description: String,
    buttonText: String,
    onTapButton: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = SutokoTypography.h1.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                color = Color(0xFF51567E)
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = description,
            textAlign = TextAlign.Center,
            fontSize = 13.5.sp,
            style = SutokoTypography.h3.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ), color = Color(0xFF272335).copy(0.85f)
            ),
            modifier = Modifier.padding(top = 6.dp)
        )
        Button(
            text = buttonText,
            onTap = onTapButton
        )
    }
}

@Composable
private fun Button(text: String, onTap: (() -> Unit)? = null) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        style = SutokoTypography.h2.copy(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            ), color = Color.White
        ),
        modifier = Modifier
            .padding(18.dp)
            .padding(top = 12.dp)
            .clip(shape = RoundedCornerShape(50))
            .background(Color(0xFF252736))
            .padding(horizontal = 18.dp, vertical = 7.dp)

            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onTap?.invoke()
            })
}
