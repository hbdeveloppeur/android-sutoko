package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class BottomNavigationSlideTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun activeBackgroundPassesBetweenTabsDuringSelectionChange() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val home = context.getString(BottomNavItem.Home.title)
        val calendar = context.getString(BottomNavItem.Calendar.title)
        compose.setContent {
            val controller = rememberNavController()
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                NavHost(controller, startDestination = BottomNavItem.Home.route) {
                    composable(BottomNavItem.Home.route) {}
                    composable(BottomNavItem.Calendar.route) {}
                    composable(BottomNavItem.Create.route) {}
                }
                BottomNavigation(
                    navController = controller,
                    onShopPressed = {},
                    backdrop = null,
                    modifier = Modifier.align(Alignment.BottomCenter).width(328.dp).testTag("dock"),
                )
            }
        }

        fun brightnessBetweenTabs(): Float {
            val pixels = compose.onNodeWithTag("dock").captureToImage().toPixelMap()
            val sidePadding = with(compose.density) { 27.dp.roundToPx() }
            val x = sidePadding + (pixels.width - 2 * sidePadding) / 4
            val y = with(compose.density) { 39.dp.roundToPx() }
            return pixels[x, y].red
        }

        compose.onNodeWithContentDescription(home).assertIsSelected()
        compose.mainClock.autoAdvance = false
        val before = brightnessBetweenTabs()
        compose.onNodeWithContentDescription(calendar).performClick()
        compose.mainClock.advanceTimeBy(112)
        val during = brightnessBetweenTabs()
        compose.mainClock.advanceTimeBy(320)
        val after = brightnessBetweenTabs()
        compose.onNodeWithContentDescription(calendar).assertIsSelected()

        assertTrue("Selection should cross between the tabs", during > before + 0.03f)
        assertTrue("The space between tabs should clear after the slide", during > after + 0.03f)
    }
}
