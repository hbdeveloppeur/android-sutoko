package fr.purpletear.sutoko.screens.main.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
internal fun rememberMainNavController(): NavHostController = key("player-only-main-navigation") {
    // Keep saved state for this graph separate from the removed developer destinations.
    rememberNavController()
}
