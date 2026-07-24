package fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation

import fr.purpletear.sutoko.R

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

    data object Companion : BottomNavItem(
        R.string.sutoko_companion,
        R.drawable.compagnon,
        "companion"
    )

    data object Shop : BottomNavItem(
        R.string.sutoko_shop,
        R.drawable.ic_shop,
        "shop"
    )
}
