package com.purpletear.game.presentation.smsgame.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.smsgame.components.dev.SmsGameDevAction
import com.purpletear.game.presentation.smsgame.components.dev.SmsGameDevCommandLine
import com.purpletear.game.presentation.smsgame.components.dev.SmsGameDevPanel

@Composable
internal fun SmsGameNavHost(
    navController: NavHostController,
    startDestination: String,
    onDebugAction: (SmsGameDevAction) -> Unit,
    builder: NavGraphBuilder.() -> Unit,
) {
    Column(Modifier.statusBarsPadding()) {
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
