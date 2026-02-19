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
import fr.purpletear.sutoko.screens.fadeComposable
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.screens.home.HomeScreen

sealed class BottomNavItem(val title: Int, val icon: Int, val route: String) {
    data object Home : BottomNavItem(
        R.string.sutoko_main_bottom_navigationbar_stories,
        R.drawable.sutoko_ic_games,
        "home"
    )

    data object Create : BottomNavItem(
        R.string.sutoko_create,
        R.drawable.sutoko_add_magic,
        "create"
    )

    data object Shop : BottomNavItem(
        R.string.sutoko_shop,
        R.drawable.ic_shop,
        "shop"
    )
}

@Composable
fun NavigationGraph(
    modifier: Modifier,
    navController: NavHostController,
    mainNavController: NavController,
    viewModel: HomeScreenViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier
            .fillMaxHeight()
            .sizeIn(maxWidth = 500.dp)
            .navigationBarsPadding()
            .then(modifier)
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                mainNavController = mainNavController,
                viewModel = viewModel
            )
        }

        fadeComposable(BottomNavItem.Create.route) {
            fr.purpletear.sutoko.screens.create.CreatePageComposable()
        }
    }
}