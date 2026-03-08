package com.purpletear.game.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun HideStatusBarEffect() {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.isStatusBarVisible = false
        systemUiController.isNavigationBarVisible = true
        onDispose {
            systemUiController.isStatusBarVisible = true
        }
    }
}
