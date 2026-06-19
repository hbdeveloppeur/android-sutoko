package com.purpletear.game.presentation.game_preview

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purpletear.core.presentation.util.openAppInStore
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.components.GamePreviewAnimatedGameTitle
import com.purpletear.game.presentation.game_preview.components.GamePreviewCategories
import com.purpletear.game.presentation.game_preview.components.GamePreviewChapterTitle
import com.purpletear.game.presentation.game_preview.components.GamePreviewDescription
import com.purpletear.game.presentation.game_preview.components.GamePreviewLabel
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnavailable
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnlockAnimation
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import com.purpletear.sutoko.game.model.game.GameCatalog
import kotlinx.coroutines.delay

/**
 * A preview screen that displays detailed game information
 */
@Composable
fun GamePreview(
    modifier: Modifier = Modifier,
    viewModel: GamePreviewViewModel,
    onNavigateToGame: (String, Boolean) -> Unit = { _, _ -> },
    onBuyGame: (GameCatalog) -> Unit = {},
    onOpenShop: () -> Unit = {},
) {
    // Get the game from the ViewModel
    val state by viewModel.game.collectAsStateWithLifecycle()
    val gameItem: GameItem? = (state as? GamePreviewUiState.Data)?.item

    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val isUserPremium by viewModel.isUserPremium.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    val showVideo = rememberShowVideoAfterNavigation(lifecycleOwner)

    Surface(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Background media: image always, video only after navigation animation
            when (val currentState = state) {
                is GamePreviewUiState.Data -> {
                    GameBackgroundPreviewMedia(
                        imageUrl = currentState.item.imageUrl,
                        videoUrl = currentState.item.videoUrl.takeIf { showVideo },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> { /* Black background from parent Box */
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

            val animationDuration = 5250L
            var unlockAnimationIsVisible by remember { mutableStateOf(false) }
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        GamePreviewEvent.PurchaseSuccess -> {
                            unlockAnimationIsVisible = true
                            delay(animationDuration)
                            unlockAnimationIsVisible = false
                        }

                        GamePreviewEvent.OpenShop -> {
                            onOpenShop()
                        }

                        GamePreviewEvent.OpenAppStore -> {
                            context.openAppInStore()
                        }

                        is GamePreviewEvent.PlayGame -> {
                            onNavigateToGame(event.gameId, event.isPurchased)
                        }

                        is GamePreviewEvent.OnBuyGameClicked -> {
                            onBuyGame(event.gameCatalog)
                        }

                        else -> {

                        }
                    }
                }
            }

            GamePreviewUnlockAnimation(isVisible = unlockAnimationIsVisible)

            var showRestartDialog by remember { mutableStateOf(false) }

            if (showRestartDialog) {
                SimpleAlertDialog(
                    onDismissRequest = { showRestartDialog = false },
                    onConfirmation = {
                        showRestartDialog = false
                        // TODO: trigger restart when ready
                    },
                    dialogTitle = stringResource(R.string.game_restart_confirm_title),
                    dialogText = stringResource(R.string.game_restart_confirm_description),
                    confirmButtonText = stringResource(R.string.game_restart_confirm_button),
                    dismissButtonText = stringResource(android.R.string.cancel),
                )
            }

            Column(
                Modifier
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .padding(vertical = 30.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp)

            ) {
                gameItem?.let { game ->
                    GamePreviewAnimatedGameTitle(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        title = game.title,
                    )
                }

                // Push remaining space
                Spacer(modifier = Modifier.weight(1f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                    // Display the current chapter title if available, otherwise show a default
                    currentChapter?.let { chapter ->
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

                    val unavailableChapter = currentChapter?.takeIf { !it.isAvailable }
                    if (unavailableChapter != null) {
                        GamePreviewUnavailable(chapter = unavailableChapter)
                    } else if (gameItem != null) {
                        GamePreviewCategories()
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {

                    if (null != gameItem) {
                        GamePreviewLabel(
                            text = stringResource(
                                if (gameItem.isFree) R.string.game_preview_free else R.string.game_preview_premium
                            ),
                            borderColor = Background.Gradient(
                                colors = listOf(
                                    Color(0xFFFECF00),
                                    Color(0xFFFF7FDF),
                                    Color(0xFF5D8BFF),
                                )
                            )
                        )
                    }

                    if (isUserPremium) {
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

                    if (true == gameItem?.isPurchased) {
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
                    avatarUrl = gameItem?.logoUrl ?: "",
                    description = gameItem?.description ?: "",
                )

                val buttonsState = viewModel.gameButtonsState

                GameActionButtons(
                    state = buttonsState,
                    modifier = Modifier.padding(bottom = 12.dp),
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


private const val NAVIGATION_ENTER_DELAY_MS = 720L

/**
 * Tracks whether background video should be shown.
 *
 * Delays showing the video until the navigation enter animation has finished,
 * hides it while the screen is not RESUMED, and ensures it stops on dispose.
 */
@Composable
private fun rememberShowVideoAfterNavigation(lifecycleOwner: LifecycleOwner): Boolean {
    var showVideo by remember { mutableStateOf(false) }

    // Enter: delay to show video after navigation animation
    LaunchedEffect(Unit) {
        delay(NAVIGATION_ENTER_DELAY_MS)
        showVideo = true
    }

    // Exit: hide video as soon as screen is not RESUMED
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState != Lifecycle.State.RESUMED) {
            showVideo = false
        } else {
            delay(NAVIGATION_ENTER_DELAY_MS)
            showVideo = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            showVideo = false
        }
    }

    return showVideo
}
