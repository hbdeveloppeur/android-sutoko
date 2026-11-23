package com.purpletear.game.presentation.game_play

import com.purpletear.ads.ReaderAds
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Trace
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_chapter_selection.chapterSelectionScreen
import com.purpletear.game.presentation.game_play.navigation.cinematicScreen
import com.purpletear.game.presentation.game_play.navigation.gameScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        ReaderAds.onInput(this, event)
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        ReaderAds.onInput(this, event)
        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Trace.beginSection("SmsGameActivity.onCreate")
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.game_presentation_fade_in,
                R.anim.game_presentation_fade_out,
            )
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.game_presentation_fade_in,
                R.anim.game_presentation_fade_out,
            )
        }

        val args = extractArgs()
        val gameId = args.gameId
        val chapterCode = args.chapterCode
        val isTrial = args.isTrial
        val autoPlay = args.autoPlay
        if (!isTrial && !(BuildConfig.DEBUG && autoPlay)) ReaderAds.track(this, gameId)

        enableEdgeToEdge()
        setContent {
            SutokoTheme {

                val navController = rememberNavController()
                val transition = remember { GameFadeTransition() }
                val readOverlayAlpha = remember { { transition.alpha } }
                val scope = rememberCoroutineScope()

                var contentRequest by remember {
                    mutableStateOf(ChapterContentRequest(requireNotNull(chapterCode)))
                }
                var showLoading by remember { mutableStateOf(false) }
                var hasLoadError by remember { mutableStateOf(false) }
                val lifecycleOwner = LocalLifecycleOwner.current
                val hideGameInput by remember {
                    derivedStateOf { transition.blocksInput || hasLoadError }
                }

                LaunchedEffect(contentRequest, lifecycleOwner) {
                    val request = contentRequest
                    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val loadingIndicator = launch {
                            delay(LOADING_INDICATOR_DELAY_MS)
                            if (!request.isCompleted) showLoading = true
                        }
                        try {
                            val ready = request.await(FIRST_CONTENT_TIMEOUT_MS)
                            loadingIndicator.cancel()
                            showLoading = false
                            if (!ready) {
                                hasLoadError = true
                            } else if (!hasLoadError && !isFinishing) {
                                transition.reveal()
                            }
                        } finally {
                            loadingIndicator.cancel()
                            showLoading = false
                        }
                    }
                }

                val fadeThenRun = remember(scope) {
                    { block: suspend () -> Unit ->
                        scope.launch {
                            if (isFinishing) return@launch
                            transition.fadeThroughBlack {
                                block()
                                !isFinishing && !hasLoadError
                            }
                        }
                    }
                }
                BackHandler(enabled = transition.blocksInput && !hasLoadError) { }

                val startDestination = if (chapterCode != null) {
                    SmsGameRoutes.game(
                        chapterCode = chapterCode,
                        isTrial = isTrial,
                        autoPlay = autoPlay,
                    )
                } else {
                    error("SmsGameActivity requires a chapterCode")
                }

                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier.fillMaxSize().then(
                            if (hideGameInput) Modifier
                                .clearAndSetSemantics { }
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent(PointerEventPass.Initial)
                                                .changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            else Modifier,
                        ),
                    ) {
                        SmsGameNavHost(
                            navController = navController,
                            startDestination = startDestination,
                            overlayAlpha = readOverlayAlpha,
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
                                        val request = ChapterContentRequest(chapterCode)
                                        contentRequest = request
                                        hasLoadError = false
                                        navController.navigate(
                                            SmsGameRoutes.game(
                                                chapterCode = chapterCode,
                                                isTrial = isTrial,
                                                autoPlay = autoPlay,
                                            )
                                        ) {
                                            popUpTo(SmsGameRoutes.GAME) { inclusive = true }
                                        }
                                        if (!request.await(FIRST_CONTENT_TIMEOUT_MS)) {
                                            hasLoadError = true
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
                                onFirstContentPlayed = { readyChapter ->
                                    contentRequest.complete(readyChapter, ready = true)
                                },
                                onLoadError = { failedChapter ->
                                    if (contentRequest.chapterCode == failedChapter) hasLoadError = true
                                    contentRequest.complete(failedChapter, ready = false)
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
                    GameLaunchStatus(
                        visible = showLoading || hasLoadError,
                        hasError = hasLoadError,
                        onExit = ::finish,
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

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.game_presentation_fade_in, R.anim.game_presentation_fade_out)
        }
    }

    private fun extractArgs(): SmsGameActivityArgs {
        val args = if (BuildConfig.DEBUG) {
            SmsGameActivityArgs.fromIntentOrExtras(intent)
        } else {
            SmsGameActivityArgs.fromIntent(intent)
        }
        return requireNotNull(args) { "SmsGameActivityArgs required" }
    }

    companion object {
        private const val FIRST_CONTENT_TIMEOUT_MS = 10_000L
        private const val LOADING_INDICATOR_DELAY_MS = 450L

        fun intent(activity: Activity, args: SmsGameActivityArgs): Intent =
            SmsGameActivityArgs.toIntent(
                Intent(activity, SmsGameActivity::class.java),
                args
            )
    }
}
