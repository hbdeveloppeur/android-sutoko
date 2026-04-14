package com.purpletear.game.presentation.game_play.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.purpletear.game.presentation.game_play.GameEngineViewModel
import com.purpletear.game.presentation.game_play.SmsGameRoutes
import com.purpletear.game.presentation.game_play.SmsGameScreen

internal fun NavGraphBuilder.gameScreen(
    gameId: String,
) = composable(
    route = SmsGameRoutes.GAME,
    arguments = listOf(
        navArgument("gameId") {
            type = NavType.StringType
            defaultValue = gameId
        },
        navArgument("chapterCode") {
            type = NavType.StringType
        },
    )
) {
    val viewModel: GameEngineViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SmsGameScreen(
        state = state,
    )
}