package com.purpletear.game.presentation.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.components.HideStatusBarEffect
import com.purpletear.game.presentation.smsgame.components.SmsGameNavHost
import com.purpletear.game.presentation.smsgame.components.descriptionScreen
import com.purpletear.game.presentation.smsgame.components.dev.SmsGameDevAction
import com.purpletear.game.presentation.smsgame.components.dev.SmsGameDevViewModel
import com.purpletear.game.presentation.smsgame.components.dev.debugPage
import com.purpletear.game.presentation.smsgame.components.navigation.gameScreen
import com.purpletear.sutoko.game.model.GameSessionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    private val viewModel: SmsGameViewModel by viewModels()
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
                }
            }
        }

        viewModel.initialize(gameId)
    }

    private fun extractGameId(): String = if (BuildConfig.DEBUG) {
        "XWA7PiyAC6e"
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
