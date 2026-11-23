package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.IconComposable
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem

internal val BottomNavigationHeight = 52.dp
internal val BottomNavigationGap = 9.dp

private val BottomNavigationShape = RoundedCornerShape(percent = 50)
private val NavigationSelectionShape = RoundedCornerShape(21.dp)
private const val SelectionSlideMillis = 240
private const val ShopIconProgress = 0.375f
private val NavigationSelection = Brush.verticalGradient(
    0f to Color(0x3DD5D5D5),
    0.48f to Color(0x2CBFBFBF),
    1f to Color(0x1EB1B1B1),
)
private val BottomTabs = listOf(BottomNavItem.Home, BottomNavItem.Calendar, BottomNavItem.Create)

@Composable
internal fun BottomNavigation(
    navController: NavController,
    onShopPressed: () -> Unit,
    backdrop: NavigationBackdrop?,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: navController.currentDestination?.route
    val selectedIndex = BottomTabs.indexOfFirst { it.route == currentRoute }
    val haptic = LocalHapticFeedback.current
    val slideDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f

    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 22.dp, end = 22.dp, bottom = BottomNavigationGap),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomNavigationHeight)
                .shadow(8.dp, BottomNavigationShape, clip = false)
                .navigationGlass(backdrop)
                .padding(horizontal = 5.dp)
                .selectableGroup(),
        ) {
            if (selectedIndex >= 0) {
                val slotWidth = maxWidth / (BottomTabs.size + 1)
                val position = animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = tween(SelectionSlideMillis),
                    label = "Bottom tab selection",
                )
                Box(
                    Modifier.align(Alignment.CenterStart)
                        .graphicsLayer { translationX = position.value * slotWidth.toPx() * slideDirection }
                        .width(slotWidth)
                        .padding(horizontal = 2.dp)
                        .height(42.dp)
                        .background(NavigationSelection, NavigationSelectionShape),
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomTabs.forEach { tab ->
                    NavigationTab(
                        tab = tab,
                        selected = tab.route == currentRoute,
                        modifier = Modifier.weight(1f),
                        onPress = onPress@{
                            if (navController.currentDestination?.route == tab.route) return@onPress
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    )
                }
                val shopLabel = stringResource(BottomNavItem.Shop.title)
                Box(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            role = Role.Button,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onShopPressed()
                        }
                        .semantics { contentDescription = shopLabel },
                    contentAlignment = Alignment.Center,
                ) {
                    val shopIcon by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_premium))
                    LottieAnimation(
                        composition = shopIcon,
                        progress = { ShopIconProgress },
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationTab(
    tab: BottomNavItem,
    selected: Boolean,
    modifier: Modifier,
    onPress: () -> Unit,
) {
    val label = stringResource(tab.title)
    val scale = if (tab == BottomNavItem.Calendar) 0.84f else 1f
    Box(
        modifier = modifier.height(48.dp)
            .selectable(
                selected = selected,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                role = Role.Tab,
                onClick = onPress,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        IconComposable(
            icon = Icon.Image(tab.icon, scaleX = scale, scaleY = scale),
            modifier = Modifier.offset(y = (-1).dp).size(26.dp),
        )
    }
}
