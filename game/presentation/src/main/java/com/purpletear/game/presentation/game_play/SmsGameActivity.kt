package com.purpletear.game.presentation.game_play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.common.components.HideStatusBarEffect
import com.purpletear.game.presentation.debug.SmsGameDevAction
import com.purpletear.game.presentation.debug.SmsGameDevViewModel
import com.purpletear.game.presentation.debug.debugPage
import com.purpletear.game.presentation.game_chapter_introduction.descriptionScreen
import com.purpletear.game.presentation.game_play.navigation.gameScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    private val viewModel: GameSessionViewModel by viewModels()
    private val devViewModel: SmsGameDevViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameId = extractGameId()

        enableEdgeToEdge()
        setContent {
            SutokoTheme {
                HideStatusBarEffect()

                val navController = rememberNavController()

                SmsGameNavHost(
                    navController = navController,
                    startDestination = SmsGameRoutes.DESCRIPTION,
                    onDebugAction = { action ->
                        when (action) {
                            SmsGameDevAction.Back -> {
                                if (!navController.popBackStack()) finish()
                            }

                            SmsGameDevAction.OpenDebugView -> {
                                if (navController.currentDestination?.route != SmsGameRoutes.DEBUG) {
                                    navController.navigate(SmsGameRoutes.debug(gameId))
                                }
                            }

                            SmsGameDevAction.Restart -> {
                                devViewModel.restart(gameId)
                            }

                            SmsGameDevAction.Update -> {
                                devViewModel.redownload(gameId)
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
                        onContinue = {
                            viewModel.currentChapterCode()?.let {
                                navController.navigate(SmsGameRoutes.game(it))
                            }
                        }
                    )

                    gameScreen(
                        gameId = gameId,
                        onNavigateToChapter = {
                            viewModel.currentChapterCode()?.let { nextCode ->
                                navController.navigate(SmsGameRoutes.game(nextCode)) {
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

    private fun extractGameId(): String = if (BuildConfig.DEBUG) {
        "taHQ3oyAtC3"
    } else {
        SmsGameActivityArgs.fromIntent(intent)?.gameId
            ?: error("Game ID required")
    }

    companion object {
        fun intent(activity: Activity, args: SmsGameActivityArgs): Intent =
            SmsGameActivityArgs.toIntent(
                Intent(activity, SmsGameActivity::class.java),
                args
            )
    }
}
