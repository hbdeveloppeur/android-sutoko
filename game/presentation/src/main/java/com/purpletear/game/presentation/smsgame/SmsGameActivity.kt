package com.purpletear.game.presentation.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.components.HideStatusBarEffect
import com.purpletear.game.presentation.smsgame.components.Filter
import com.purpletear.game.presentation.smsgame.components.SmsGameDescription
import com.purpletear.game.presentation.smsgame.components.SmsGameScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.GameSessionState
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for playing SMS-style games.
 * Receives the game parameters via intent extras using [SmsGameActivityModel].
 */
@AndroidEntryPoint
class SmsGameActivity : AppCompatActivity() {

    private val viewModel: SmsGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model = SmsGameActivityModel.fromIntent(this)
        val gameId = model?.gameId ?: DEFAULT_GAME_ID
        val isGranted = model?.isGranted ?: DEFAULT_IS_GRANTED

        if (model == null) {
            Log.w(TAG, "SmsGameActivityModel not found in intent extras, using default test values")
        }

        enableEdgeToEdge()

        setContent {
            SutokoTheme {
                HideStatusBarEffect()
                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                Box {
                    if (sessionState is GameSessionState.Ready) {
                        val chapter = (sessionState as GameSessionState.Ready).chapter
                        SmsGameNavHost(
                            navController = navController,
                            chapter = chapter,
                            gameId = gameId
                        )
                    }
                    AnimatedVisibility(visible = sessionState is GameSessionState.Loading) {
                        Filter()
                    }
                }
            }
        }

        viewModel.initialize(gameId, isGranted)
    }

    companion object {
        private const val TAG = "SmsGameActivity"

        // Default test values when launching directly as main activity
        private const val DEFAULT_GAME_ID = "XWA7PiyAC6e"
        private const val DEFAULT_IS_GRANTED = true

        /**
         * Creates an Intent to launch SmsGameActivity with the specified parameters.
         *
         * @param activity The activity to use for creating the intent
         * @param model The model containing game parameters
         * @return An Intent configured to launch SmsGameActivity
         */
        fun require(activity: Activity, model: SmsGameActivityModel): Intent {
            return Intent(activity, SmsGameActivity::class.java).apply {
                putExtra(SmsGameActivityModel.extraKey, model)
            }
        }
    }
}

private object SmsGameRoutes {
    const val DESCRIPTION = "description"
    const val GAME = "game"
}

@Composable
private fun SmsGameNavHost(
    navController: NavHostController,
    chapter: Chapter,
    gameId: String,
) {
    NavHost(
        navController = navController,
        startDestination = SmsGameRoutes.DESCRIPTION,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(SmsGameRoutes.DESCRIPTION) {
            SmsGameDescription(
                number = chapter.number,
                title = chapter.title,
                description = chapter.description,
                onContinueButtonClicked = {
                    navController.navigate(SmsGameRoutes.GAME)
                },
            )
        }
        composable(SmsGameRoutes.GAME) {
            val playViewModel: SmsGamePlayViewModel = hiltViewModel()
            
            // Initialize the view model when entering the game screen
            LaunchedEffect(Unit) {
                playViewModel.initialize(gameId, chapter.code)
            }
            
            SmsGameScreen(
                viewModel = playViewModel,
                onLoadNextChapter = {
                    playViewModel.onNextChapter()
                },
            )
        }
    }
}
