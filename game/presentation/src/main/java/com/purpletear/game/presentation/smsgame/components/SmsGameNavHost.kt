package com.purpletear.game.presentation.smsgame.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
internal fun SmsGameNavHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) = NavHost(
    navController = navController,
    startDestination = startDestination,
    enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
    exitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) },
    builder = builder,
)
