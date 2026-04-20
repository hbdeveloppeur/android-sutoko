package com.purpletear.game.presentation.game_play.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
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
    onNavigateToChapter: () -> Unit,
) = composable(
    route = SmsGameRoutes.GAME,
    enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
    exitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) },
    popEnterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
    popExitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) },
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

    LaunchedEffect(viewModel) {
        viewModel.navigateToNextChapter.collect {
            onNavigateToChapter()
        }
    }

    SmsGameScreen(
        state = state,
        onNextChapterClick = viewModel::onNextChapterClicked,
    )
}