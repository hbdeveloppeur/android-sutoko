package com.purpletear.game.presentation.game_play.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.purpletear.sutoko.game.model.scene.Scene
import com.purpletear.game.presentation.game_play.GameEngineViewModel
import com.purpletear.game.presentation.game_play.SmsGameRoutes
import com.purpletear.game.presentation.game_play.SmsGameScreen
import com.purpletear.game.presentation.game_play.GameContentReadiness
import com.purpletear.game.presentation.game_play.contentReadiness

internal fun NavGraphBuilder.gameScreen(
    gameId: String,
    onNavigateToChapter: (String) -> Unit,
    onNavigateToCinematic: () -> Unit,
    onNavigateToBuy: () -> Unit,
    onNavigateToExit: () -> Unit,
    onFirstContentPlayed: (String) -> Unit = {},
    onLoadError: (String) -> Unit = {},
) = composable(
    route = SmsGameRoutes.GAME,
    enterTransition = {
        if (initialState.destination.route == SmsGameRoutes.GAME) EnterTransition.None
        else fadeIn(tween(500, easing = FastOutSlowInEasing))
    },
    exitTransition = {
        if (targetState.destination.route == SmsGameRoutes.GAME) ExitTransition.None
        else fadeOut(tween(360, easing = FastOutSlowInEasing))
    },
    popEnterTransition = { fadeIn(tween(500, easing = FastOutSlowInEasing)) },
    popExitTransition = { fadeOut(tween(360, easing = FastOutSlowInEasing)) },
    arguments = listOf(
        navArgument("gameId") {
            type = NavType.StringType
            defaultValue = gameId
        },
        navArgument("chapterCode") {
            type = NavType.StringType
        },
        navArgument(SmsGameRoutes.IS_TRIAL_ARG) {
            type = NavType.BoolType
            defaultValue = false
        },
        navArgument(SmsGameRoutes.AUTO_PLAY_ARG) {
            type = NavType.BoolType
            defaultValue = false
        },
    )
) { entry ->
    val chapterCode = requireNotNull(entry.arguments?.getString("chapterCode"))
    val viewModel: GameEngineViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<android.app.Activity>().firstOrNull()
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToNextChapter.collect { chapterCode ->
            onNavigateToChapter(chapterCode)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToCinematic.collect {
            onNavigateToCinematic()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToBuy.collect {
            onNavigateToBuy()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToExit.collect {
            onNavigateToExit()
        }
    }

    var readyScene by remember(viewModel) { mutableStateOf<Scene?>(null) }
    val contentReadiness = state.contentReadiness(sceneReady = state.currentScene == null || readyScene == state.currentScene)
    LaunchedEffect(viewModel, contentReadiness) {
        when (contentReadiness) {
            GameContentReadiness.Ready -> onFirstContentPlayed(chapterCode)
            GameContentReadiness.Failed -> onLoadError(chapterCode)
            GameContentReadiness.Waiting -> Unit
        }
    }

    SmsGameScreen(
        state = state,
        onSceneReady = { readyScene = it },
        onNextChapterClick = { viewModel.onNextChapterClicked(activity) },
        onBackClick = viewModel::onBackClicked,
        onVocalClick = viewModel::onVocalClicked,
        onChoiceSelected = viewModel::onChoiceSelected,
        onRevealChoicesClicked = viewModel::onRevealChoicesClicked,
        onHideChoicesClicked = viewModel::onHideChoicesClicked,
        onMangaPageDismissed = viewModel::onMangaPageDismissed,
        onToggleChoicesDarkMode = viewModel::onToggleChoicesDarkMode,
        onFakeNotificationDismissed = viewModel::onFakeNotificationDismissed,
        onVisualNovelDismissed = viewModel::onVisualNovelDismissed,
        onVisualNovelDialogSound = viewModel::playVisualNovelDialogSound,
        onHoldPauseChanged = viewModel::onHoldPauseChanged,
        onImageViewerVisibilityChanged = viewModel::onImageViewerVisibilityChanged,
        onAdvanceOnTap = viewModel::onAdvanceOnTap,
    )
}
