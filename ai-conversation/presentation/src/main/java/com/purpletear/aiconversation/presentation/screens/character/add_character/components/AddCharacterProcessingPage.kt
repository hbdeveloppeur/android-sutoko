package com.purpletear.aiconversation.presentation.screens.character.add_character.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.aiconversation.domain.enums.Gender
import com.purpletear.aiconversation.domain.enums.ProcessStatus
import com.purpletear.aiconversation.domain.model.Media
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl
import com.purpletear.aiconversation.presentation.component.button.ButtonComposable
import com.purpletear.aiconversation.presentation.component.button.ButtonTheme
import com.purpletear.aiconversation.presentation.component.character.avatar.character_avatar.CharacterAvatar
import kotlinx.coroutines.delay


@Composable
internal fun AddCharacterProcessingPage(
    modifier: Modifier = Modifier,
    name: String,
    processState: ProcessStatus,
    gender: Gender,
    media: Media?,
    onClickContinue: () -> Unit
) {

    Box(
        modifier = Modifier
            .then(modifier)
            .background(Color(0xFF0b0c11)),
        contentAlignment = Alignment.Center
    ) {
        SnowImage()
        HeaderImage(
            gender = gender
        )
        Column(
            Modifier.widthIn(min = 320.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar(
                gender = gender,
                media = media,
            )
            CharacterName(state = processState, name = name)
            StateText(state = processState)
            Spacer(Modifier.height(32.dp))
            Button(state = processState, onClick = onClickContinue)
        }
    }
}

@Composable
private fun Button(state: ProcessStatus, onClick: () -> Unit) {
    val isDone = !arrayOf(
        ProcessStatus.INITIAL,
        ProcessStatus.PROCESSING,
        ProcessStatus.PENDING,
    ).contains(state)
    ButtonComposable(
        Modifier.alpha(
            if (isDone) 1f else 0f
        ),
        theme = ButtonTheme.WhitePill(),
        title = "Continuer",
        onClick = onClick
    )
}

@Composable
private fun HeaderImage(gender: Gender) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.07f),
        contentAlignment = Alignment.TopCenter
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth(1f),
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    "https://data.sutoko.app/resources/sutoko-ai/image/default-${gender.code}.jpeg"
                )
                .crossfade(1280)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun StateText(state: ProcessStatus) {

    val isVisible = remember {
        mutableStateOf(false)
    }

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isVisible.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = 720,
            easing = LinearOutSlowInEasing
        ), label = "mutableHeightAnimated"
    )

    LaunchedEffect(Unit) {
        delay(2800)
        isVisible.value = true
    }

    val text = when (state) {
        ProcessStatus.COMPLETED -> {
            "Votre personnage a été créé"
        }

        ProcessStatus.FAILED -> {
            "Une erreur est survenue, veuillez réessayer plus tard."
        }

        else -> {
            "Création du personnage..."
        }
    }

    Text(
        modifier = Modifier.alpha(alphaAnimation),
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontStyle = FontStyle.Italic,
        color = Color.White
    )
}

@Composable
private fun CharacterName(state: ProcessStatus, name: String) {
    val isVisibleAvatar = remember {
        mutableStateOf(false)
    }

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isVisibleAvatar.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = 720,
            easing = LinearOutSlowInEasing
        ), label = "mutableHeightAnimated"
    )

    LaunchedEffect(Unit) {
        delay(2000)
        isVisibleAvatar.value = true
    }


    Row(
        modifier = Modifier.alpha(alphaAnimation),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            modifier = Modifier.size(22.dp),
            painter = painterResource(id = R.drawable.ic_moon),
            contentDescription = null,
        )
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = Color.White)
        Box(
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                ProcessStatus.COMPLETED -> {
                    LoaderLottieAnimation()
                }

                ProcessStatus.FAILED -> {
                }

                else -> {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Center)
                                .alpha(1f),
                            color = Color.LightGray,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoaderLottieAnimation(modifier: Modifier = Modifier) {
    val rawRes = R.raw.loader_checked
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    Box(
        modifier = Modifier
            .size(32.dp)
            .clipToBounds()
            .then(modifier),
        contentAlignment = Alignment.Center

    ) {
        LottieAnimation(
            modifier = Modifier
                .size(32.dp),
            composition = composition,
            clipSpec = LottieClipSpec.Frame(
                min = 0,
                max = 38,
                maxInclusive = true
            )

        )
    }
}


@Composable
private fun Avatar(gender: Gender, media: Media?) {
    val isVisibleAvatar = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(1280)
        isVisibleAvatar.value = true
    }


    AnimatedVisibility(
        visible = isVisibleAvatar.value,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)) +
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 500)),
    ) {
        CharacterAvatar(
            modifier = Modifier.size(52.dp),
            orUrl = if (media == null) {
                "https://data.sutoko.app/resources/sutoko-ai/image/default-${gender.code}.jpeg"
            } else {
                getRemoteAssetsUrl(media.url)
            },
            isSelected = false,
            size = 48.dp
        )
    }
}

@Composable
private fun SnowImage() {
    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.05f),
        model =
        ImageRequest.Builder(LocalContext.current)
            .data("https://data.sutoko.app/resources/sutoko-ai/image/snow-bg.jpg")
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
}