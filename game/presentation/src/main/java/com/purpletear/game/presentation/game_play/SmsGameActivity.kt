package com.purpletear.game.presentation.game_play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.debug.SmsGameDevAction
import com.purpletear.game.presentation.debug.SmsGameDevViewModel
import com.purpletear.game.presentation.debug.debugPage
import com.purpletear.game.presentation.game_chapter_introduction.descriptionScreen
import com.purpletear.game.presentation.game_chapter_selection.chapterSelectionScreen
import com.purpletear.game.presentation.game_play.navigation.gameScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    private val viewModel: GameSessionViewModel by viewModels()
    private val devViewModel: SmsGameDevViewModel by viewModels()
    @Inject
    lateinit var storyTestingCoordinator: StoryTestingCoordinator

    private var isTestMode: Boolean = false
    private var storyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = extractArgs()
        val gameId = args.gameId
        isTestMode = args.isTestMode
        storyId = args.storyId

        if (isTestMode) {
            storyId?.let { storyTestingCoordinator.startTesting(gameId, it) }
        }

        enableEdgeToEdge()
        setContent {
            SutokoTheme {

                val navController = rememberNavController()
                val overlayAlpha = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()

                val fadeThenRun = remember(scope) {
                    { block: () -> Unit ->
                        scope.launch {
                            overlayAlpha.animateTo(1f, tween(500))
                            block()
                            delay(280)
                            overlayAlpha.animateTo(0f, tween(durationMillis = 500))
                        }
                    }
                }

                val startDestination = if (isTestMode) {
                    SmsGameRoutes.game("test", isTestMode = true)
                } else {
                    SmsGameRoutes.DESCRIPTION
                }

                SmsGameNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    overlayAlpha = overlayAlpha.value,
                    onDebugAction = { action ->
                        when (action) {
                            SmsGameDevAction.Back -> {
                                if (!navController.popBackStack()) finish()
                            }

                            SmsGameDevAction.OpenDebugView -> {
                                if (navController.currentDestination?.route != SmsGameRoutes.DEBUG) {
                                    fadeThenRun {
                                        navController.navigate(SmsGameRoutes.debug(gameId))
                                    }
                                }
                            }

                            SmsGameDevAction.Restart -> {
                                devViewModel.restart(gameId)
                            }

                            SmsGameDevAction.Update -> {
                                devViewModel.redownload(gameId)
                            }

                            SmsGameDevAction.Delete -> {
                                devViewModel.delete(gameId) { finish() }
                            }
                        }
                    }
                ) {
                    debugPage(
                        gameId = gameId,
                        viewModel = viewModel,
                    )

                    descriptionScreen(
                        viewModel = viewModel,
                        onContinue = { chapterCode ->
                            fadeThenRun {
                                navController.navigate(SmsGameRoutes.game(chapterCode, isTestMode))
                            }
                        },
                        onSelectChapter = {
                            val state = viewModel.sessionState.value
                            val currentChapterCode =
                                (state as? com.purpletear.sutoko.game.model.GameSessionState.Ready)?.chapter?.code
                                    ?: ""
                            fadeThenRun {
                                navController.navigate(
                                    SmsGameRoutes.chapterSelection(currentChapterCode)
                                )
                            }
                        }
                    )

                    if (BuildConfig.DEBUG) {
                        chapterSelectionScreen(
                            gameId = gameId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    gameScreen(
                        gameId = gameId,
                        onNavigateToChapter = { chapterCode ->
                            fadeThenRun {
                                navController.navigate(SmsGameRoutes.game(chapterCode, isTestMode)) {
                                    popUpTo(SmsGameRoutes.GAME) { inclusive = true }
                                }
                            }
                        },
                    )
                }
            }
        }

        viewModel.initialize(gameId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTestMode) {
            storyTestingCoordinator.stopTesting()
        }
    }

    private fun extractArgs(): SmsGameActivityArgs {
        return SmsGameActivityArgs.fromIntent(intent)
            ?: error("SmsGameActivityArgs required")
    }

    companion object {
        fun intent(activity: Activity, args: SmsGameActivityArgs): Intent =
            SmsGameActivityArgs.toIntent(
                Intent(activity, SmsGameActivity::class.java),
                args
            )
    }
}
