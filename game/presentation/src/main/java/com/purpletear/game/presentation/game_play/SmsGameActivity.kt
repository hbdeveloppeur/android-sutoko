package com.purpletear.game.presentation.game_play

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Trace
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import com.purpletear.game.presentation.game_chapter_selection.chapterSelectionScreen
import com.purpletear.game.presentation.game_play.navigation.cinematicScreen
import com.purpletear.game.presentation.game_play.navigation.gameScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

@AndroidEntryPoint
class SmsGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Trace.beginSection("SmsGameActivity.onCreate")
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        }

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
                val readOverlayAlpha = remember { { overlayAlpha.value } }
                val scope = rememberCoroutineScope()

                var contentRequest by remember {
                    mutableStateOf(ChapterContentRequest(requireNotNull(chapterCode)))
                }
                var showLoading by remember { mutableStateOf(false) }
                var hasLoadError by remember { mutableStateOf(false) }
                var isTransitioning by remember { mutableStateOf(false) }
                val transitionMutex = remember { Mutex() }
                val lifecycleOwner = LocalLifecycleOwner.current
                val hideGameInput by remember {
                    derivedStateOf { isTransitioning || overlayAlpha.value > 0f || hasLoadError }
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
                            } else if (!isTransitioning && !hasLoadError && !isFinishing) {
                                withFrameNanos { }
                                overlayAlpha.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                )
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
                            if (!transitionMutex.tryLock()) return@launch
                            try {
                                if (isFinishing) return@launch
                                isTransitioning = true
                                overlayAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
                                block()
                                if (!isFinishing && !hasLoadError) {
                                    withFrameNanos { }
                                    overlayAlpha.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
                                }
                            } finally {
                                isTransitioning = false
                                transitionMutex.unlock()
                            }
                        }
                    }
                }
                BackHandler(enabled = isTransitioning) { }

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
            overridePendingTransition(0, 0)
        }
    }

    private fun extractArgs(): SmsGameActivityArgs {
        return SmsGameActivityArgs.fromIntentOrExtras(intent)
            ?: error("SmsGameActivityArgs required")
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
