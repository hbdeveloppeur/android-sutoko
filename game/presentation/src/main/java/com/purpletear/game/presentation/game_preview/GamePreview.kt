package com.purpletear.game.presentation.game_preview

import android.content.Context
import android.content.Intent
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
import com.purpletear.core.presentation.components.icon.Icon.Image
import com.purpletear.core.presentation.util.openAppInStore
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.GameLogo
import com.purpletear.game.presentation.common.components.NickNameInputDialog
import com.purpletear.game.presentation.game_play.components.Avatar
import com.purpletear.game.presentation.game_preview.components.GamePreviewButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewCategories
import com.purpletear.game.presentation.game_preview.components.GamePreviewChapterTitle
import com.purpletear.game.presentation.game_preview.components.GamePreviewDescription
import com.purpletear.game.presentation.game_preview.components.GamePreviewFavoriteButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewGradients
import com.purpletear.game.presentation.game_preview.components.GamePreviewLabel
import com.purpletear.game.presentation.game_preview.components.GamePreviewOptionsButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewShareButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnavailable
import com.purpletear.game.presentation.game_preview.components.GamePreviewUnlockAnimation
import com.purpletear.game.presentation.game_preview.components.GamePreviewVersionBadges
import com.purpletear.game.presentation.game_preview.components.PremiumActiveLabelGradient
import com.purpletear.game.presentation.game_preview.components.PremiumLabelGradient
import com.purpletear.game.presentation.game_preview.components.UnlockedLabelGradient
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.model.GameActionState
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.formatNarrativeThemes
import com.purpletear.game.presentation.model.toGameActionState
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import com.purpletear.sutoko.game.model.FriendzonedLegacyIds
import kotlinx.coroutines.delay
import com.example.sharedelements.R as SutokoSharedElementsR

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
    onNavigateToChapters: (String) -> Unit = {},
    onOpenAccountConnection: () -> Unit = {},
    onOpenOptions: (String) -> Unit = {},
) {
    // Get the game from the ViewModel
    val state by viewModel.game.collectAsStateWithLifecycle()
    val gameItem: GameItem? = (state as? GamePreviewUiState.Data)?.item

    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val isUserPremium by viewModel.isUserPremium.collectAsStateWithLifecycle()
    val isUserConnected by viewModel.isUserConnected.collectAsStateWithLifecycle()
    val isOptionsVisible by viewModel.isOptionsVisible.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()
    val isPurchaseLoading by viewModel.isPurchaseLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val releasedChaptersCount by viewModel.releasedChaptersCount.collectAsStateWithLifecycle()
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
            // Friendzoned games may have advanced their own progress while
            // this screen sat in the back stack: refresh the current chapter.
            viewModel.onResume()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
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
                                .padding(vertical = 30.dp, horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(26.dp)
                        ) {
                            gameItem?.let { game ->
                                GameLogo(
                                    titleUrl = game.titleUrl,
                                    contentDescription = game.title,
                                    modifier = Modifier
                                        .padding(top = 40.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .fillMaxWidth(0.8f)
                                        .heightIn(max = 140.dp),
                                )
                            }

                            // Push remaining space
                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {

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

                                val unavailableChapter =
                                    currentChapter?.takeIf { !it.available && !isAdmin }
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
                                                    .semantics {
                                                        contentDescription = avatarDescription
                                                    },
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


                            val gameActionState = gameItem?.toGameActionState(
                                isPurchasing = isPurchasing,
                                isPurchaseLoading = isPurchaseLoading,
                                currentChapter = currentChapter,
                                appBuildNumber = appBuildNumber,
                                isUserConnected = isUserConnected,
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                GameActionButtons(
                                    gameActionState = gameActionState,
                                    onAction = viewModel::onAction,
                                )

                                // The catalog count is a server-side cached value
                                // that can be stale; real loaded chapters win.
                                val chaptersCount = releasedChaptersCount
                                    ?: (state as? GamePreviewUiState.Data)?.gameCatalog?.chaptersCount
                                    ?: 0
                                // Friendzoned games manage their own progress: chapter
                                // switching from the preview would have no effect on them.
                                val isFriendzoned =
                                    FriendzonedLegacyIds.isFriendzoned(gameItem?.legacyId)
                                if (gameActionState is GameActionState.Play && chaptersCount > 0 && !isFriendzoned) {
                                    GamePreviewButton(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        title = stringResource(R.string.game_presentation_game_story_chapters_button_chapters),
                                        subtitle = stringResource(
                                            R.string.game_presentation_game_story_chapters_button_chapters_count,
                                            chaptersCount,
                                        ),
                                        onClick = {
                                            gameItem.let { onNavigateToChapters(it.id) }
                                        },
                                        icon = Image(
                                            drawableId = SutokoSharedElementsR.drawable.shared_elements_shared_ic_arrow_back_ios,
                                            scaleX = -1f,
                                        ),
                                        background = Background.Solid(Color.White.copy(alpha = 0.12f)),
                                    )
                                }
                            }
                        }
                    }
                }

                gameItem?.let { game ->
                    if (isAdmin) {
                        GamePreviewVersionBadges(
                            currentVersion = game.localVersion,
                            availableVersion = game.version,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(top = 16.dp, start = 8.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 8.dp, end = 8.dp),
                    ) {
                        GamePreviewShareButton(
                            onShare = { shareGame(context, game) },
                        )
                        GamePreviewFavoriteButton(
                            isFavorite = game.isFavorite,
                            onToggle = { viewModel.onAction(GamePreviewAction.OnToggleFavorite) },
                        )
                        if (isOptionsVisible) {
                            GamePreviewOptionsButton(
                                onClick = { onOpenOptions(game.id) },
                            )
                        }
                    }
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
 * Opens the system share sheet with the public deep link of [game].
 */
private fun shareGame(context: Context, game: GameItem) {
    val message = context.getString(
        R.string.game_presentation_game_preview_share_message,
        game.title,
        GamePreviewDeepLink.url(game.id),
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, null))
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