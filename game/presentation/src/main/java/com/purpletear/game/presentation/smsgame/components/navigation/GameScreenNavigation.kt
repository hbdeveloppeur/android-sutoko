package com.purpletear.game.presentation.smsgame.components.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.purpletear.game.presentation.smsgame.SmsGamePlayViewModel
import com.purpletear.game.presentation.smsgame.SmsGameRoutes
import com.purpletear.game.presentation.smsgame.components.SmsGameScreen

internal fun NavGraphBuilder.gameScreen(
    gameId: String,
    chapterCode: String,
) = composable(
    route = SmsGameRoutes.GAME,
    arguments = listOf(
        navArgument("gameId") {
            type = NavType.StringType
            defaultValue = gameId
        },
        navArgument("chapterCode") {
            type = NavType.StringType
            defaultValue = chapterCode
        },
    )
) {
    val viewModel: SmsGamePlayViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SmsGameScreen(
        state = state,
    )
}