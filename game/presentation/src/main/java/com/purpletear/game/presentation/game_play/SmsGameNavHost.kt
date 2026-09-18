package com.purpletear.game.presentation.game_play

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
internal fun SmsGameNavHost(
    navController: NavHostController,
    startDestination: String,
    overlayAlpha: () -> Float,
    builder: NavGraphBuilder.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val currentEntry by navController.currentBackStackEntryAsState()
        LaunchedEffect(currentEntry) {
            currentEntry?.destination?.route?.let { route ->
                Log.d("SmsGameNavHost", "Current screen: $route")
            }
        }
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(tween(500, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(tween(500, easing = FastOutSlowInEasing)) },
            builder = builder,
        )
        val isOverlayVisible by remember(overlayAlpha) {
            derivedStateOf { overlayAlpha() > 0f }
        }
        if (isOverlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = overlayAlpha() }
                    .background(Color.Black)
            )
        }
    }
}
