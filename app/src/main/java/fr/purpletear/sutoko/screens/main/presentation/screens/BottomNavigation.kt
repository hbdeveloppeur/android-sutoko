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
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sharedelements.theme.SutokoTypography
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.IconComposable
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem

@Composable
fun BottomNavigation(
    navController: NavController,
    onShopPressed: () -> Unit,
) {
    val height = 92.dp

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF110D11))
                .height(height)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(height)
                    .widthIn(min = 300.dp)
                    .fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Games (Home) Tab
                NavButton(
                    modifier = Modifier.weight(1f),
                    icon = Icon.Image(
                        R.drawable.sutoko_ic_games,
                        offsetY = -12,
                    ),
                    iconHeight = 22.dp,
                    label = BottomNavItem.Home.title,
                    isSelected = currentRoute == BottomNavItem.Home.route,
                    onPress = {
                        if (currentRoute != BottomNavItem.Home.route) {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                // Create Tab
                NavButton(
                    modifier = Modifier.weight(1f),
                    icon = Icon.Image(
                        R.drawable.sutoko_add_magic,
                        offsetY = -10,
                    ),
                    iconHeight = 22.dp,
                    label = BottomNavItem.Create.title,
                    isSelected = currentRoute == BottomNavItem.Create.route,
                    onPress = {
                        if (currentRoute != BottomNavItem.Create.route) {
                            navController.navigate(BottomNavItem.Create.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                // Shop Button (always 100% alpha, external navigation)
                ShopButton(
                    modifier = Modifier.weight(1f),
                    onPress = onShopPressed
                )
            }
        }
    }
}

@Composable
private fun NavButton(
    modifier: Modifier = Modifier,
    icon: Icon,
    iconHeight: Dp,
    label: Int,
    isSelected: Boolean,
    onPress: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .alpha(if (isSelected) 1f else 0.7f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPress()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(modifier = Modifier.size(iconHeight)) {
            IconComposable(icon)
        }

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

@Composable
private fun ShopButton(
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .alpha(1f) // Always 100% alpha as requested
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPress()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(modifier = Modifier.size(22.dp)) {
            IconComposable(
                Icon.LottieAnimation(
                    R.raw.lottie_premium,
                    offsetY = -12,
                    iteration = 1000,
                    scaleX = 1.7f,
                    scaleY = 1.7f,
                )
            )
        }

        GoldGradientText(
            text = stringResource(id = BottomNavItem.Shop.title),
        )
    }
}

@Composable
private fun GoldGradientText(
    text: String,
    modifier: Modifier = Modifier
) {
    val goldColors = listOf(
        Color(0xFFFDE08F),
        Color(0xFFF6C469),
        Color(0xFFB8860B),
        Color(0xFFF6C469),
        Color(0xFFFDE08F)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "goldGradientTransition")

    val gradientShift by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3500,
                easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientShift"
    )

    var textSize by remember { mutableStateOf(IntSize.Zero) }

    val brush = remember(gradientShift, textSize) {
        val width = textSize.width.toFloat().coerceAtLeast(1f)
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
