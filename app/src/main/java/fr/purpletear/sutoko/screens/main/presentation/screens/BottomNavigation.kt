package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sharedelements.theme.SutokoTypography
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.IconComposable
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem

@Composable
fun BottomNavigation(
    navController: NavController,
    onCreatePressed: () -> Unit,
    onShopPressed: () -> Unit,
) {
    val height = 92.dp
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF110D11))
                .height(height)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            var currentMenu by remember { mutableStateOf(BottomNavItem.Home.icon) }
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(height)
                    .widthIn(min = 300.dp)
                    .fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    modifier = Modifier
                        .weight(1f),
                    icon = Icon.Image(
                        R.drawable.sutoko_ic_games,
                        offsetY = -12,
                    ),
                    iconHeight = 22.dp,
                    label = BottomNavItem.Home.title,
                    isSelected = currentMenu == BottomNavItem.Home.icon,
                    onPress = onPress@{
                        if (currentMenu == BottomNavItem.Home.icon) {
                            return@onPress
                        }
                        // Add little pleasant vibration
                        currentMenu = BottomNavItem.Home.icon
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )

                Button(
                    modifier = Modifier
                        .weight(1f),
                    icon = Icon.Image(
                        R.drawable.sutoko_ic_menu_users, offsetY = -10,
                    ),
                    iconHeight = 22.dp,
                    label = BottomNavItem.Create.title,
                    isSelected = currentMenu == BottomNavItem.Create.icon,
                    onPress = {
                        currentMenu = BottomNavItem.Create.icon
                        // Add little pleasant vibration
                        onCreatePressed()
                    }
                )

                Button(
                    modifier = Modifier.weight(1f),
                    icon = Icon.LottieAnimation(
                        R.raw.lottie_premium,
                        offsetY = -12,
                        iteration = 1000,
                        scaleX = 1.7f,
                        scaleY = 1.7f,
                    ),
                    iconHeight = 22.dp,
                    label = BottomNavItem.Shop.title,
                    isSelected = true,
                    animatedText = true,
                    onPress = {
                        onShopPressed()
                    }
                )

            }
        }
    }
}

@Composable
private fun Button(
    modifier: Modifier = Modifier,
    icon: Icon,
    iconHeight: Dp,
    label: Int,
    isSelected: Boolean,
    animatedText: Boolean = false,
    onPress: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .alpha(if (isSelected) 1f else 0.7f)
            .then(modifier)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                // subtle pleasant haptic on tap
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPress()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(modifier = Modifier.size(iconHeight)) {
            IconComposable(icon)
        }

        if (animatedText) {
            GoldGradientText(
                text = stringResource(id = label),
            )
        } else {
            Text(
                text = stringResource(id = label),
                fontSize = 10.sp,
                style = SutokoTypography.body1.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
    }
}

@Composable
fun GoldGradientText(
    text: String,
    modifier: Modifier = Modifier
) {
    // Colors for a gold-like gradation
    val goldColors = listOf(
        Color(0xFFFDE08F),
        Color(0xFFF6C469),
        Color(0xFFB8860B),
        Color(0xFFF6C469),
        Color(0xFFFDE08F)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "goldGradientTransition")

    // Animate horizontal shift of the gradient
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3500,
                easing = CubicBezierEasing(0.4f, 0f, 1f, 1f) // ease-in style
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientShift"
    )

    // Use size to calculate gradient width relative to text
    var textSize by remember { mutableStateOf(IntSize.Zero) }

    val brush = remember(gradientShift, textSize) {
        val width = textSize.width.toFloat().coerceAtLeast(1f) // guard division by zero
        val startX = (gradientShift - 1f) * width
        val endX = gradientShift * width

        Brush.linearGradient(
            colors = goldColors,
            start = Offset(startX, 0f),
            end = Offset(endX, textSize.height.toFloat())
        )
    }

    Text(
        text = text,
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                textSize = layoutCoordinates.size
            },
        fontSize = 10.sp,
        style = SutokoTypography.body1.copy(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            ),
            brush = brush
        )
    )
}