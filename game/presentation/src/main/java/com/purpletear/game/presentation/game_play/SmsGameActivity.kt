package com.purpletear.game.presentation.game_play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.common.components.HideStatusBarEffect
import com.purpletear.game.presentation.debug.SmsGameDevAction
import com.purpletear.game.presentation.debug.SmsGameDevViewModel
import com.purpletear.game.presentation.debug.debugPage
import com.purpletear.game.presentation.game_chapter_introduction.descriptionScreen
import com.purpletear.game.presentation.game_play.navigation.gameScreen
import com.purpletear.sutoko.game.model.GameSessionState
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

                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
                val memories by remember { viewModel.getMemories() }.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                if (sessionState is GameSessionState.Ready) {
                    val readyState = sessionState as GameSessionState.Ready
                    val chapter = readyState.chapter
                    val totalChapters = readyState.totalChapters

                    SmsGameNavHost(
                        navController = navController,
                        startDestination = SmsGameRoutes.description(chapter.normalizedCode),
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
                            gameSessionState = sessionState,
                            memories = memories
                        )

                        descriptionScreen(
                            gameId = gameId,
                            totalChapters = totalChapters,
                            onContinue = {
                                navController.navigate(SmsGameRoutes.GAME)
                            }
                        )

                        gameScreen(
                            gameId = gameId,
                            chapterCode = chapter.normalizedCode
                        )
                    }
                } else if (sessionState is GameSessionState.Error) {
                    val errorState = sessionState as GameSessionState.Error

                    // TODO : Display error
                    LaunchedEffect(errorState) {
                        Toast.makeText(
                            applicationContext,
                            errorState.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
