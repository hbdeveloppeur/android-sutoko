package fr.purpletear.sutoko.screens.main.presentation.screens.home.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sharedelements.theme.SutokoTypography
import com.purpletear.game_presentation.components.AnimatedGradientBorderBox
import com.purpletear.game_presentation.components.GradientThemes
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.presentation.util.LogCompositions


@Composable
fun AiConversationCard(
    messagesCount: Int?,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    isAvailable: Boolean
) {
    LogCompositions(name = "AiConversationCard", level = 3)

    Box(
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
            .background(Color.Black.copy(0.3f))
            .then(modifier)
            .clickable {
                onTap()
            }) {

        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://data.sutoko.app/resources/card_aiconv.jpg?t=1")
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }
        GamePreviewUnlockAnimation(isVisible = isVisible)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAvailable) Color.Black.copy(0.1f) else Color.Black.copy(0.6f))
        )

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleAnimation(Modifier)
        }


        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Row {
                Text(
                    text = stringResource(R.string.app_ai_conversation_card_title),
                    fontSize = 16.sp,
                    style = TextStyle(
                        letterSpacing = 0.5.sp,
                        color = Color.White,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontFamily = FontFamily(Font(R.font.font_poppins_bold, FontWeight.Bold))
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(0.7f),
                text = stringResource(R.string.app_ai_conversation_card_subtitle),
                fontSize = 12.sp, style = SutokoTypography.h3.copy(
                    letterSpacing = 0.5.sp,
                    lineHeight = 16.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {


                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.app_ai_conversation_card_tag),
                    color = Color(0xFFE8BDF1),
                    fontSize = 12.sp,
                    style = SutokoTypography.h3.copy(
                        letterSpacing = 0.5.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )

            }
        }
    }
}

@Composable
private fun CircleAnimation(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://assets4.lottiefiles.com/private_files/lf30_qgah66oi.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        modifier = Modifier
            .offset(x = (-60).dp, y = 10.dp)
            .size(120.dp)
            .alpha(0.3f)
            .then(modifier),
        composition = composition,
        progress = { progress },
    )
}

/**
 * A composable that displays an animated gradient border box with fade in/out animation.
 *
 * @param modifier Modifier to be applied to the layout
 * @param isVisible Controls the visibility of the animation with a fade effect
 * @param animationDurationMillis Duration of the fade animation in milliseconds
 */
@Composable
internal fun GamePreviewUnlockAnimation(
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    animationDurationMillis: Int = 500
) {
    // Animate the alpha value based on isVisible parameter
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "visibilityAnimation"
    )

    Box(
        Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        AnimatedGradientBorderBox(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            borderRadius = 0.dp,
            borderWidth = 2.dp,
            theme = GradientThemes.Cool
        )
    }
}
