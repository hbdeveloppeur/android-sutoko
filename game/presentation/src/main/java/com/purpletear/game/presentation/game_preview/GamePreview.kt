package com.purpletear.game.presentation.game_preview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.core.presentation.util.openAppInStore
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.NickNameInputDialog
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.game.presentation.common.components.GameLogo
import com.purpletear.game.presentation.game_preview.components.GamePreviewCategories
import com.purpletear.game.presentation.game_preview.components.GamePreviewChapterTitle
import com.purpletear.game.presentation.game_preview.components.GamePreviewDescription
import com.purpletear.game.presentation.game_preview.components.GamePreviewFavoriteButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewGradients
import com.purpletear.game.presentation.game_preview.components.GamePreviewLabel
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnavailable
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnlockAnimation
import com.purpletear.game.presentation.game_preview.components.PremiumActiveLabelGradient
import com.purpletear.game.presentation.game_preview.components.PremiumLabelGradient
import com.purpletear.game.presentation.game_preview.components.UnlockedLabelGradient
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.formatNarrativeThemes
import com.purpletear.game.presentation.model.toGameActionState
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import kotlinx.coroutines.delay

/**
 * A preview screen that displays detailed game information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePreview(
    modifier: Modifier = Modifier,
    viewModel: GamePreviewViewModel,
    fallbackBackgroundPainter: Painter? = null,
    onNavigateToGame: (String, Int?, Boolean, String?, Boolean) -> Unit = { _, _, _, _, _ -> },
    onOpenAccountConnection: () -> Unit = {},
) {
    // Get the game from the ViewModel
    val state by viewModel.game.collectAsStateWithLifecycle()
    val gameItem: GameItem? = (state as? GamePreviewUiState.Data)?.item

    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val isUserPremium by viewModel.isUserPremium.collectAsStateWithLifecycle()
    val isUserConnected by viewModel.isUserConnected.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()
    val isPurchaseLoading by viewModel.isPurchaseLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val appBuildNumber = viewModel.appBuildNumber

    val showVideo = rememberShowVideoAfterNavigation()

    val transitionAlpha = remember { Animatable(0f) }
    var isFadingToGame by remember { mutableStateOf(false) }
    var showAuthorAvatar by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            transitionAlpha.snapTo(0f)
            isFadingToGame = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
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

                    GamePreviewUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.LightGray,
                                strokeWidth = 2.dp,
                            )
                        }
                    }

                    GamePreviewUiState.NotFound -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.game_presentation_story_unavailable),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                    }

                    is GamePreviewUiState.Error -> { /* Black background from parent Box */
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
                var showAlreadyBoughtDialog by remember { mutableStateOf(false) }
                // Non-null => the nickname dialog is visible; the Boolean carries the trial
                // intent (OnTry vs OnPlay) so it is echoed back to the VM on confirm.
                var nickNameDialogIsTrial by remember { mutableStateOf<Boolean?>(null) }
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
                                isFadingToGame = true
                                transitionAlpha.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                                onNavigateToGame(
                                    event.gameId,
                                    event.legacyId,
                                    event.isPurchased,
                                    event.chapterCode,
                                    event.isTrial,
                                )
                            }

                            is GamePreviewEvent.RequestNickName -> {
                                nickNameDialogIsTrial = event.isTrial
                            }

                            GamePreviewEvent.ShowRestartDialog -> {
                                showRestartDialog = true
                            }

                            GamePreviewEvent.OpenAccountConnection -> {
                                onOpenAccountConnection()
                            }

                            GamePreviewEvent.ShowAlreadyBoughtAlert -> {
                                showAlreadyBoughtDialog = true
                            }

                            is GamePreviewEvent.ShowError -> Unit
                        }
                    }
                }

                GamePreviewUnlockAnimation(isVisible = unlockAnimationIsVisible)

                nickNameDialogIsTrial?.let { isTrial ->
                    NickNameInputDialog(
                        onConfirm = {
                            nickNameDialogIsTrial = null
                            viewModel.onNickNameConfirmed(it, isTrial)
                        },
                        onDismiss = { nickNameDialogIsTrial = null },
                    )
                }

                if (showAlreadyBoughtDialog) {
                    SimpleAlertDialog(
                        onDismissRequest = { showAlreadyBoughtDialog = false },
                        onConfirmation = { showAlreadyBoughtDialog = false },
                        dialogTitle = stringResource(R.string.game_presentation_already_bought_alert_title),
                        dialogText = stringResource(R.string.game_presentation_already_bought_alert_description),
                        confirmButtonText = stringResource(R.string.game_presentation_already_bought_alert_button),
                    )
                }

                if (showRestartDialog) {
                    SimpleAlertDialog(
                        onDismissRequest = { showRestartDialog = false },
                        onConfirmation = {
                            showRestartDialog = false
                            viewModel.onAction(GamePreviewAction.OnRestartConfirm)
                        },
                        dialogTitle = stringResource(R.string.game_presentation_game_restart_confirm_title),
                        dialogText = stringResource(R.string.game_presentation_game_restart_confirm_description),
                        confirmButtonText = stringResource(R.string.game_presentation_game_restart_confirm_button),
                        dismissButtonText = stringResource(android.R.string.cancel),
                    )
                }

                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val viewportHeight = this.maxHeight
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = viewportHeight)
                                .navigationBarsPadding()
                                .statusBarsPadding()
                                .padding(vertical = 30.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(26.dp)
                        ) {
                            gameItem?.let { game ->
                                GameLogo(
                                    titleUrl = game.titleUrl,
                                    contentDescription = game.title,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .fillMaxWidth(0.8f)
                                        .heightIn(max = 120.dp),
                                )
                            }

                            // Push remaining space
                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {

                                // Display the current chapter title if available, otherwise show a default
                                currentChapter?.let { chapter ->
                                    GamePreviewChapterTitle(
                                        text = stringResource(
                                            R.string.game_presentation_game_preview_chapter_title,
                                            chapter.number,
                                            chapter.title
                                        )
                                    )
                                }
                                    ?: GamePreviewChapterTitle(text = stringResource(R.string.game_presentation_game_preview_loading_chapter))

                                val unavailableChapter = currentChapter?.takeIf { !it.isAvailable }
                                if (unavailableChapter != null) {
                                    GamePreviewUnavailable(
                                        chapter = unavailableChapter
                                    )
                                } else if (gameItem != null) {
                                    GamePreviewCategories(
                                        categories = formatNarrativeThemes(
                                            gameItem.narrativeThemes,
                                            stringResource(R.string.game_presentation_game_card_genre_fallback)
                                        )
                                    )
                                }
                            }

                            if (gameItem != null && !gameItem.isOfficial) {
                                gameItem.author?.let { author ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.game_presentation_game_preview_written_by),
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp,
                                            fontFamily = PlusJakartaSansFontFamily,
                                        )
                                        gameItem.authorAvatarUrl?.let { avatarUrl ->
                                            val avatarDescription =
                                                stringResource(R.string.game_presentation_game_preview_author_avatar)
                                            Avatar(
                                                modifier = Modifier
                                                    .background(Color.White, CircleShape)
                                                    .clip(CircleShape)
                                                    .clickable { showAuthorAvatar = true }
                                                    .semantics { contentDescription = avatarDescription },
                                                size = 22.dp,
                                                borderWidth = 1.4.dp,
                                                borderColor = Color.White,
                                                imageModel = avatarUrl,
                                            )
                                        }

                                        Text(
                                            text = author.displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = PlusJakartaSansFontFamily,
                                        )
                                        if (!author.isCertified) {
                                            CertifiedIcon(Color(0xFF2799D7))
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {

                                if (gameItem != null) {
                                    GamePreviewLabel(
                                        text = stringResource(
                                            if (gameItem.isFree) R.string.game_presentation_game_preview_free else R.string.game_presentation_game_preview_premium
                                        ),
                                        borderColor = Background.Gradient(colors = PremiumLabelGradient)
                                    )
                                }

                                if (isUserPremium) {
                                    GamePreviewLabel(
                                        text = stringResource(R.string.game_presentation_game_preview_premium_active),
                                        borderColor = Background.Gradient(colors = PremiumActiveLabelGradient)
                                    )
                                }

                                if (gameItem?.isPurchased == true) {
                                    GamePreviewLabel(
                                        text = stringResource(R.string.game_presentation_game_preview_unlocked),
                                        textColor = Color(0xFFADFFA1),
                                        borderColor = Background.Gradient(colors = UnlockedLabelGradient)
                                    )
                                }

                                if (gameItem?.isOfficial == false) {
                                    GamePreviewLabel(
                                        text = stringResource(R.string.game_presentation_game_preview_community)
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
                                    isUserConnected = isUserConnected,
                                ),
                                onAction = viewModel::onAction,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                    }
                }

                gameItem?.let { game ->
                    GamePreviewFavoriteButton(
                        isFavorite = game.isFavorite,
                        onToggle = { viewModel.onAction(GamePreviewAction.OnToggleFavorite) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 8.dp, end = 8.dp),
                    )
                }

                gameItem?.authorAvatarUrl?.let { avatarUrl ->
                    AuthorAvatarOverlay(
                        visible = showAuthorAvatar,
                        imageModel = avatarUrl,
                        onDismiss = { showAuthorAvatar = false },
                    )
                }

                if (isFadingToGame || transitionAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(transitionAlpha.value)
                            .background(Color.Black)
                    )
                }
            }
        }
    }
}


/**
 * Full-screen overlay showing the author's avatar enlarged.
 * Scrim only fades; the avatar pops in with a calm, non-bouncy spring.
 * Dismissed by tapping the scrim, the avatar, or the back button.
 */
@Composable
private fun AuthorAvatarOverlay(
    visible: Boolean,
    imageModel: Any?,
    onDismiss: () -> Unit,
) {
    BackHandler(enabled = visible, onBack = onDismiss)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ),
        exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.92f),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Avatar(
                modifier = Modifier
                    .shadow(elevation = 24.dp, shape = CircleShape)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss),
                size = 200.dp,
                borderWidth = 1.5.dp,
                borderColor = Color.White,
                imageModel = imageModel,
            )
        }
    }
}

@Composable
private fun CertifiedIcon(color: Color) {
    Icon(
        painter = painterResource(id = R.drawable.game_presentation_author_ic_certified),
        contentDescription = stringResource(R.string.game_presentation_game_preview_certified_author),
        modifier = Modifier.size(16.dp),
        tint = color,
    )
}