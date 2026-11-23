package com.purpletear.game.presentation.game_play.navigation

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

) { backStackEntry ->
    val gameEntry = remember(backStackEntry) {
        navController.getBackStackEntry(SmsGameRoutes.GAME)
    }
    val viewModel: GameEngineViewModel = hiltViewModel(gameEntry)
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.onReaderResumed()
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CinematicScreen(
        body = state.cinematicBody,
        autoSkip = viewModel.isAutoPlay,
        loadScene = viewModel::loadScene,
        onFinished = {
            viewModel.onCinematicFinished()
            onExit()
        },
    )
}
