package com.purpletear.game.presentation.game_play.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.purpletear.game.presentation.game_play.CinematicScreen
import com.purpletear.game.presentation.game_play.GameEngineViewModel
import com.purpletear.game.presentation.game_play.SmsGameRoutes

/**
 * Cinematic destination. It shares the `gameScreen`'s [GameEngineViewModel] by scoping to the game
 * destination's back-stack entry, so the engine (paused at `[intro=start]`) and the extracted body
 * are the same instance the SMS screen owns.
 */
internal fun NavGraphBuilder.cinematicScreen(
    navController: NavHostController,
    onExit: () -> Unit,
) = composable(
    route = SmsGameRoutes.CINEMATIC,
    enterTransition = { fadeIn(tween(500, easing = FastOutSlowInEasing)) },
    exitTransition = { fadeOut(tween(360, easing = FastOutSlowInEasing)) },
    popEnterTransition = { fadeIn(tween(500, easing = FastOutSlowInEasing)) },
    popExitTransition = { fadeOut(tween(360, easing = FastOutSlowInEasing)) },
) { backStackEntry ->
    val gameEntry = remember(backStackEntry) {
        navController.getBackStackEntry(SmsGameRoutes.GAME)
    }
    val viewModel: GameEngineViewModel = hiltViewModel(gameEntry)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CinematicScreen(
        body = state.cinematicBody,
        loadScene = viewModel::loadScene,
        onFinished = {
            viewModel.onCinematicFinished()
            onExit()
        },
    )
}
