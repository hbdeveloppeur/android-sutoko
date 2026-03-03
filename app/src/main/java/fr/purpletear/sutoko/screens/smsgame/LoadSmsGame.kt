package fr.purpletear.sutoko.screens.smsgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Empty screen for loading SMS games.
 * This screen is displayed when the user clicks "Open" on a game.
 *
 * @param gameId The ID of the game to load
 * @param onNavigateBack Callback to navigate back to the previous screen
 */
@Composable
fun LoadSmsGameScreen(
    gameId: String,
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Empty screen - to be implemented
    }
}
