package com.purpletear.game.presentation.game_play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Trace
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.game_chapter_selection.chapterSelectionScreen
import com.purpletear.game.presentation.game_play.navigation.cinematicScreen
import com.purpletear.game.presentation.game_play.navigation.gameScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Trace.beginSection("SmsGameActivity.onCreate")
        super.onCreate(savedInstanceState)

        val args = extractArgs()
        val gameId = args.gameId
        val chapterCode = args.chapterCode
        val isTrial = args.isTrial
        val autoPlay = args.autoPlay

        enableEdgeToEdge()
        setContent {
            SutokoTheme {

                val navController = rememberNavController()
                val overlayAlpha = remember { Animatable(1f) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    overlayAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }

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

                val startDestination = if (chapterCode != null) {
                    SmsGameRoutes.game(
                        chapterCode = chapterCode,
                        isTrial = isTrial,
                        autoPlay = autoPlay,
                    )
                } else {
                    error("SmsGameActivity requires a chapterCode")
                }

                SmsGameNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    overlayAlpha = overlayAlpha.value,
                ) {
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
                                navController.navigate(
                                    SmsGameRoutes.game(
                                        chapterCode = chapterCode,
                                        isTrial = isTrial,
                                        autoPlay = autoPlay,
                                    )
                                ) {
                                    popUpTo(SmsGameRoutes.GAME) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToCinematic = {
                            fadeThenRun {
                                navController.navigate(SmsGameRoutes.cinematic())
                            }
                        },
                        onNavigateToBuy = {
                            fadeThenRun { finish() }
                        },
                        onNavigateToExit = {
                            fadeThenRun { finish() }
                        },
                    )

                    cinematicScreen(
                        navController = navController,
                        onExit = {
                            fadeThenRun {
                                navController.popBackStack()
                            }
                        },
                    )
                }
            }
        }

        Trace.endSection()
    }

    override fun onDestroy() {
        Trace.beginSection("SmsGameActivity.onDestroy")
        super.onDestroy()
        Trace.endSection()
    }

    private fun extractArgs(): SmsGameActivityArgs {
        return SmsGameActivityArgs.fromIntentOrExtras(intent)
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
