package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun translucentDockNavigatesAndKeepsShopAsAnAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val games = context.getString(BottomNavItem.Home.title)
        val calendar = context.getString(BottomNavItem.Calendar.title)
        var shopOpens = 0

        compose.setContent {
            val controller = rememberNavController()
            val backdrop = rememberNavigationBackdrop()
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().then(backdrop?.capture() ?: Modifier).background(Color.DarkGray)) {
                    NavHost(controller, startDestination = BottomNavItem.Home.route) {
                        composable(BottomNavItem.Home.route) { Text("Games destination") }
                        composable(BottomNavItem.Calendar.route) { Text("Calendar destination") }
                        composable(BottomNavItem.Create.route) { Text("Create destination") }
                    }
                }
                BottomNavigation(
                    navController = controller,
                    onShopPressed = { shopOpens++ },
                    backdrop = backdrop,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        compose.onNodeWithContentDescription(games).assertIsSelected()
        compose.onNodeWithContentDescription(calendar).performClick().assertIsSelected()
        compose.onNodeWithContentDescription(games).assertIsNotSelected()
        compose.onNodeWithText("Calendar destination").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.sutoko_shop)).performClick()
        compose.runOnIdle { assertEquals(1, shopOpens) }
        compose.onNodeWithContentDescription(calendar).assertIsSelected()
    }
}
