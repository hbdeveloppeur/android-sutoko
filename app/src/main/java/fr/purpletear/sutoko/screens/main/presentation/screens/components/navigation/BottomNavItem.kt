package fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.screens.calendar.CalendarScreen
import fr.purpletear.sutoko.screens.main.presentation.screens.community.CommunityScreen
import fr.purpletear.sutoko.screens.main.presentation.screens.home.HomeScreen

sealed class BottomNavItem(var title: Int, var icon: Int, var route: String) {
    object Home : BottomNavItem(
        R.string.sutoko_main_bottom_navigationbar_stories,
        R.drawable.sutoko_ic_games,
        "home"
    )

    object Calendar : BottomNavItem(R.string.coming_soon, R.drawable.ic_apps, "calendar")
    object Community : BottomNavItem(
        R.string.sutoko_main_bottom_navigationbar_sommunity,
        R.drawable.sutoko_ic_menu_users,
        "community"
    )

    object Create : BottomNavItem(
        R.string.sutoko_main_bottom_navigationbar_sommunity,
        R.drawable.sutoko_add_magic,
        "create"
    )

    object Shop : BottomNavItem(R.string.sutoko_shop, R.drawable.ic_shop, "shop")
}

@Composable
fun NavigationGraph(
    modifier: Modifier,
    navController: NavHostController,
    mainNavController: NavController,
    viewModel: HomeScreenViewModel,
) {
    NavHost(
        navController, modifier = Modifier
            .fillMaxHeight()
            .sizeIn(maxWidth = 500.dp)
            .navigationBarsPadding()
            .then(modifier), startDestination = BottomNavItem.Home.route
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                mainNavController = mainNavController,
                viewModel = viewModel
            )
        }

        composable(BottomNavItem.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel
            )
        }

        composable(BottomNavItem.Community.route) {
            CommunityScreen(
                viewModel = viewModel
            )
        }
    }
}