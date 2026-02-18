package com.purpletear.game.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.components.AnimatedGameTitle
import com.purpletear.game.presentation.components.GameBackgroundPreviewMedia
import com.purpletear.game.presentation.components.GamePreviewButtonsWrapper
import com.purpletear.game.presentation.components.GamePreviewCategories
import com.purpletear.game.presentation.components.GamePreviewChapterTitle
import com.purpletear.game.presentation.components.GamePreviewDescription
import com.purpletear.game.presentation.components.GamePreviewLabel
import com.purpletear.game.presentation.components.GamePreviewUnavailable
import com.purpletear.game.presentation.components.GamePreviewUnlockAnimation
import com.purpletear.game.presentation.components.PositionedCircularGradient
import com.purpletear.game.presentation.components.VerticalGradient
import com.purpletear.game.presentation.sealed.Background
import com.purpletear.game.presentation.viewmodels.GamePreviewViewModel
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.Game
import kotlinx.coroutines.delay

/**
 * A preview screen that displays detailed game information
 */
@Composable
fun GamePreview(
    modifier: Modifier = Modifier,
    onNavigateToGame: (Int, Boolean) -> Unit = { _, _ -> },
    onBuyGame: (Game) -> Unit = {},
    onOpenChapters: (Game, List<Chapter>) -> Unit,
    onOpenShop: () -> Unit = {},
    viewModel: GamePreviewViewModel = hiltViewModel()
) {
    // Get the game from the ViewModel
    val game: Game? = viewModel.game.value
    val currentChapter = viewModel.currentChapter.collectAsState()
    val isGameBought = viewModel.isGameBought
    val isUserPremium = viewModel.isUserPremium

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()


    // Call onResume when the screen is resumed
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onResume()
        }
    }


    // Show header only after the navigation enter animation has finished (760ms)
    var showVideo by remember { mutableStateOf(false) }
    // Enter: delay to show header after navigation animation
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(720)
        showVideo = true
    }
    // Exit: hide header as soon as screen is not RESUMED (start of exit transition)
    LaunchedEffect(lifecycleState) {
        if (lifecycleState != Lifecycle.State.RESUMED) {
            showVideo = false
        } else {
            // When returning to this screen (RESUMED), show the video again after the enter delay
            kotlinx.coroutines.delay(720)
            showVideo = true
        }
    }



    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    viewModel.stopMenuSound()
                }

                Lifecycle.Event.ON_START -> {
                    // App coming back to foreground
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // Call stopMenuSound when the composable is disposed (navigation changes or composable is closed)
    DisposableEffect(Unit) {
        onDispose {
            // Ensure background video stops immediately before leaving the page
            showVideo = false
            viewModel.stopMenuSound()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Use the new GameBackgroundPreviewMedia component
            game?.let { game ->
                if (showVideo && game.videoUrl != null) {
                    GameBackgroundPreviewMedia(
                        game = game,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (game.videoUrl == null) {
                    GameBackgroundPreviewMedia(
                        game = game,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Get screen dimensions to make translation values adaptable
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp
            val screenHeight = configuration.screenHeightDp

            Gradients(
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            val unlockEventFlow = viewModel.gameBoughtEvents
            val animationDuration = 5250L
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(unlockEventFlow) {
                unlockEventFlow.collect {
                    isVisible = true
                    delay(animationDuration)
                    isVisible = false
                }
            }

            // Collect play game events
            val playGameEventFlow = viewModel.playGameEvents
            LaunchedEffect(playGameEventFlow) {
                playGameEventFlow.collect {
                    viewModel.game.value?.id?.let { gameId ->
                        onNavigateToGame(gameId, viewModel.isGameBought.value)
                    }
                }
            }

            // Collect buy game events
            val buyGameEventFlow = viewModel.buyGameEvents
            LaunchedEffect(buyGameEventFlow) {
                buyGameEventFlow.collect { game ->
                    onBuyGame(game)
                }
            }

            // Collect open chapters events
            val openChaptersEventFlow = viewModel.openChaptersEvents
            LaunchedEffect(openChaptersEventFlow) {
                openChaptersEventFlow.collect { (game, chapters) ->
                    onOpenChapters(game, chapters)
                }
            }

            // Collect open shop events
            val openShopEventFlow = viewModel.openShopEvents
            LaunchedEffect(openShopEventFlow) {
                openShopEventFlow.collect {
                    onOpenShop()
                }
            }

            GamePreviewUnlockAnimation(isVisible = isVisible)

            Column(
                Modifier
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .padding(vertical = 30.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp)

            ) {
                game?.let { game ->
                    AnimatedGameTitle(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        title = game.metadata.title,
                    )
                }

                // Push remaining space
                Spacer(modifier = Modifier.weight(1f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                    // Display the current chapter title if available, otherwise show a default
                    currentChapter.value?.let { chapter ->
                        GamePreviewChapterTitle(
                            text = stringResource(
                                R.string.game_preview_chapter_title,
                                chapter.number,
                                chapter.title
                            )
                        )
                    } ?: run {
                        GamePreviewChapterTitle(text = stringResource(R.string.game_preview_loading_chapter))
                    }

                    if (currentChapter.value != null && !currentChapter.value!!.isAvailable) {
                        GamePreviewUnavailable(chapter = currentChapter.value!!)
                    } else if (game != null && game.metadata.categories.isNotEmpty()) {
                        GamePreviewCategories(game = game)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {

                    if (null != game && game.isPremium) {
                        GamePreviewLabel(
                            text = stringResource(R.string.game_preview_premium),
                            borderColor = Background.Gradient(
                                colors = listOf(
                                    Color(0xFFFECF00),
                                    Color(0xFFFF7FDF),
                                    Color(0xFF5D8BFF),
                                )
                            )
                        )
                    } else if (null != game) {
                        GamePreviewLabel(
                            text = stringResource(R.string.game_preview_free),
                            borderColor = Background.Gradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFFFFF),
                                )
                            )
                        )
                    }


                    if (isUserPremium.value) {
                        GamePreviewLabel(
                            text = stringResource(R.string.game_preview_premium_active),
                            borderColor = Background.Gradient(
                                colors = listOf(
                                    Color(0xFFFECF00),
                                    Color(0xFFFF7FDF),
                                    Color(0xFF3B30E7),
                                )
                            )
                        )
                    }

                    if (isGameBought.value) {
                        GamePreviewLabel(
                            text = stringResource(R.string.game_preview_unlocked),
                            textColor = Color(0xFFADFFA1),
                            borderColor = Background.Gradient(
                                colors = listOf(
                                    Color(0xFF3D753A),
                                    Color(0xFF51FF40),
                                    Color(0xFF50A44D),
                                )
                            )
                        )
                    }
                }

                GamePreviewDescription(
                    avatarUrl = viewModel.gameSquareLogoURL.value ?: "",
                    description = game?.metadata?.description ?: "",
                )

                GamePreviewButtonsWrapper(
                    Modifier.padding(bottom = 12.dp),
                    viewModel = viewModel,
                )
            }
        }
    }
}


@Composable
private fun Gradients(
    screenWidth: Int = 0,
    screenHeight: Int = 0,
) {
    Box(Modifier.fillMaxSize()) {
        // Left positioned gradient
        PositionedCircularGradient(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            translationXFactor = -2f,
            alpha = 0.2f
        )

        // Right positioned gradient
        PositionedCircularGradient(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            translationXFactor = 2f,
            alpha = 0.1f
        )

        VerticalGradient(
            modifier = Modifier
                .height(160.dp)
                .align(Alignment.TopCenter)
        )
    }
}
