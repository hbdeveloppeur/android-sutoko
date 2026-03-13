package com.purpletear.game.presentation.smsgame.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.purpletear.game.presentation.smsgame.SmsGameRoutes

@Composable
internal fun SmsGameNavHost(
    navController: NavHostController,
    builder: NavGraphBuilder.() -> Unit
) = NavHost(
    navController = navController,
    startDestination = SmsGameRoutes.DESCRIPTION,
    enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
    exitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) },
    builder = builder,
)
