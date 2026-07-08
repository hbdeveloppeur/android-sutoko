package com.purpletear.game.presentation.game_preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purpletear.core.presentation.util.openAppInStore
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.NickNameInputDialog
import com.purpletear.game.presentation.game_preview.components.GamePreviewAnimatedGameTitle
import com.purpletear.game.presentation.game_preview.components.GamePreviewCategories
import com.purpletear.game.presentation.game_preview.components.GamePreviewChapterTitle
import com.purpletear.game.presentation.game_preview.components.GamePreviewDescription
import com.purpletear.game.presentation.game_preview.components.GamePreviewGradients
import com.purpletear.game.presentation.game_preview.components.GamePreviewLabel
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnavailable
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnlockAnimation
import com.purpletear.game.presentation.game_preview.components.PremiumActiveLabelGradient
import com.purpletear.game.presentation.game_preview.components.PremiumLabelGradient
import com.purpletear.game.presentation.game_preview.components.UnlockedLabelGradient
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.toGameActionState
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import kotlinx.coroutines.delay

/**
 * A preview screen that displays detailed game information
 */
@Composable
fun GamePreview(
    modifier: Modifier = Modifier,
    viewModel: GamePreviewViewModel,
    fallbackBackgroundPainter: Painter? = null,
    onNavigateToGame: (String, Int?, Boolean) -> Unit = { _, _, _ -> },
) {
    // Get the game from the ViewModel
    val state by viewModel.game.collectAsStateWithLifecycle()
    val gameItem: GameItem? = (state as? GamePreviewUiState.Data)?.item

    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val isUserPremium by viewModel.isUserPremium.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()
    val isPurchaseLoading by viewModel.isPurchaseLoading.collectAsStateWithLifecycle()
    val appBuildNumber = viewModel.appBuildNumber

    val showVideo = rememberShowVideoAfterNavigation()

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
                        imageUrl = currentState.item.menuBackgroundUrl?.takeIf { it.isNotBlank() },
                        videoUrl = currentState.item.videoUrl.takeIf { showVideo && it?.isNotBlank() == true },
                        fallbackPainter = fallbackBackgroundPainter.takeIf { currentState.item.videoUrl.isNullOrBlank() },
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

            GamePreviewGradients(
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            val animationDuration = 5250L
            var unlockAnimationIsVisible by remember { mutableStateOf(false) }
            var showRestartDialog by remember { mutableStateOf(false) }
            var showNickNameDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current

            var wasDownloading by remember { mutableStateOf(false) }
            val downloadProgress = gameItem?.downloadProgress
            LaunchedEffect(downloadProgress) {
                if (downloadProgress != null) {
                    wasDownloading = true
                } else if (wasDownloading) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    wasDownloading = false
                }
            }

            LaunchedEffect(Unit) {
                viewModel.start()
            }

            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        GamePreviewEvent.PurchaseSuccess -> {
                            unlockAnimationIsVisible = true
                            delay(animationDuration)
                            unlockAnimationIsVisible = false
                        }

                        GamePreviewEvent.OpenAppStore -> {
                            context.openAppInStore()
                        }

                        is GamePreviewEvent.PlayGame -> {
                            onNavigateToGame(event.gameId, event.legacyId, event.isPurchased)
                        }

                        GamePreviewEvent.RequestNickName -> {
                            showNickNameDialog = true
                        }

                        GamePreviewEvent.ShowRestartDialog -> {
                            showRestartDialog = true
                        }

                        is GamePreviewEvent.ShowError -> Unit
                    }
                }
            }

            GamePreviewUnlockAnimation(isVisible = unlockAnimationIsVisible)

            if (showNickNameDialog) {
                NickNameInputDialog(
                    onConfirm = {
                        showNickNameDialog = false
                        viewModel.onNickNameConfirmed(it)
                    },
                    onDismiss = { showNickNameDialog = false },
                )
            }

            if (showRestartDialog) {
                SimpleAlertDialog(
                    onDismissRequest = { showRestartDialog = false },
                    onConfirmation = {
                        showRestartDialog = false
                        viewModel.onAction(GamePreviewAction.OnRestartConfirm)
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
                    }
                        ?: GamePreviewChapterTitle(text = stringResource(R.string.game_preview_loading_chapter))

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

                    if (gameItem != null) {
                        GamePreviewLabel(
                            text = stringResource(
                                if (gameItem.isFree) R.string.game_preview_free else R.string.game_preview_premium
                            ),
                            borderColor = Background.Gradient(colors = PremiumLabelGradient)
                        )
                    }

                    if (isUserPremium) {
                        GamePreviewLabel(
                            text = stringResource(R.string.game_preview_premium_active),
                            borderColor = Background.Gradient(colors = PremiumActiveLabelGradient)
                        )
                    }

                    if (gameItem?.isPurchased == true) {
                        GamePreviewLabel(
                            text = stringResource(R.string.game_preview_unlocked),
                            textColor = Color(0xFFADFFA1),
                            borderColor = Background.Gradient(colors = UnlockedLabelGradient)
                        )
                    }
                }

                GamePreviewDescription(
                    avatarUrl = gameItem?.logoUrl ?: "",
                    description = gameItem?.description ?: "",
                )


                GameActionButtons(
                    gameActionState = gameItem?.toGameActionState(
                        isPurchasing = isPurchasing,
                        isPurchaseLoading = isPurchaseLoading,
                        currentChapter = currentChapter,
                        appBuildNumber = appBuildNumber,
                    ),
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }
}
