package com.purpletear.game.presentation.game_play

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.debug.SmsGameDevAction
import com.purpletear.game.presentation.debug.SmsGameDevCommandLine
import com.purpletear.game.presentation.debug.SmsGameDevPanel

@Composable
internal fun SmsGameNavHost(
    navController: NavHostController,
    startDestination: String,
    onDebugAction: (SmsGameDevAction) -> Unit,
    builder: NavGraphBuilder.() -> Unit,
) {
    Column(Modifier.statusBarsPadding()) {
        val currentEntry by navController.currentBackStackEntryAsState()
        LaunchedEffect(currentEntry) {
            currentEntry?.destination?.route?.let { route ->
                Log.d("SmsGameNavHost", "Current screen: $route")
            }
        }

        if (BuildConfig.DEBUG) {
            SmsGameDevPanel(onAction = onDebugAction)
            SmsGameDevCommandLine(
                onCommand = { command ->
                    when (command) {
                        "/restart" -> onDebugAction(SmsGameDevAction.Restart)
                        "/update" -> onDebugAction(SmsGameDevAction.Update)
                    }
                }
            )
        }
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) },
            builder = builder,
        )
    }
}
