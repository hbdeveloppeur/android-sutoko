package fr.purpletear.sutoko.screens.main.presentation

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationStateTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun removedDeveloperDestinationIsNotRestoredIntoPlayerGraph() {
        val restoration = StateRestorationTester(compose)
        var includeDeveloperDestination = true
        lateinit var controller: NavHostController
        val removedRoute = "game_preview_options/story"
        restoration.setContent {
            controller = if (includeDeveloperDestination) rememberNavController() else rememberMainNavController()
            NavHost(controller, startDestination = MainScreenPages.Home.route) {
                composable(MainScreenPages.Home.route) { Text("Home") }
                if (includeDeveloperDestination) composable(removedRoute) { Text("Developer controls") }
            }
        }
        compose.runOnIdle { controller.navigate(removedRoute) }
        compose.runOnIdle { assertEquals(removedRoute, controller.currentDestination?.route) }
        includeDeveloperDestination = false
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { assertEquals(MainScreenPages.Home.route, controller.currentDestination?.route) }
    }

    @Test
    fun normalPlayerNavigationSurvivesStateRestoration() {
        val restoration = StateRestorationTester(compose)
        lateinit var controller: NavHostController
        restoration.setContent {
            controller = rememberMainNavController()
            NavHost(controller, startDestination = MainScreenPages.Home.route) {
                composable(MainScreenPages.Home.route) { Text("Home") }
                composable(MainScreenPages.GamePreview.route) { Text("Story") }
            }
        }
        compose.runOnIdle { controller.navigate(MainScreenPages.GamePreview.createRoute("story")) }
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { assertEquals(MainScreenPages.GamePreview.route, controller.currentDestination?.route) }
    }
}
