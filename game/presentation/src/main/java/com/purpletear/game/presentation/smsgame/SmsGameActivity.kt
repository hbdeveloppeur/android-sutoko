package com.purpletear.game.presentation.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.components.HideStatusBarEffect
import com.purpletear.game.presentation.smsgame.components.descriptionScreen
import com.purpletear.game.presentation.smsgame.components.gameScreen
import com.purpletear.game.presentation.smsgame.components.SmsGameNavHost
import com.purpletear.sutoko.game.model.GameSessionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    private val viewModel: SmsGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameId = extractGameId()

        enableEdgeToEdge()
        setContent {
            SutokoTheme {
                HideStatusBarEffect()

                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                if (sessionState is GameSessionState.Ready) {
                    val readyState = sessionState as GameSessionState.Ready
                    val chapter = readyState.chapter
                    val totalChapters = readyState.totalChapters
                    
                    SmsGameNavHost(
                        navController = navController,
                        startDestination = SmsGameRoutes.description(chapter.code)
                    ) {
                        descriptionScreen(
                            gameId = gameId,
                            totalChapters = totalChapters,
                            onContinue = {
                                navController.navigate(SmsGameRoutes.GAME)
                            }
                        )
                        gameScreen(
                            gameId = gameId,
                            chapter = chapter,
                            onNextChapter = { chapterCode ->
                                navController.navigate(SmsGameRoutes.description(chapterCode)) {
                                    popUpTo(SmsGameRoutes.description(chapterCode)) { inclusive = true }
                                }
                            }
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
