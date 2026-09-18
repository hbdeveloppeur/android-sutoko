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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.purpletear.game.presentation.game_preview.components.GamePreviewConnectButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewDescription
import com.purpletear.game.presentation.game_preview.components.GamePreviewFavoriteButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewGradients
import com.purpletear.game.presentation.game_preview.components.GamePreviewLabel
import com.purpletear.game.presentation.game_preview.components.GamePreviewMenuSoundEffect
import com.purpletear.game.presentation.game_preview.components.GamePreviewOptionsButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewShareButton
import com.purpletear.game.presentation.game_preview.components.GamePreviewSoundButton
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
import com.purpletear.sutoko.game.model.game.GameDownloadState
import kotlinx.coroutines.delay
import com.example.sharedelements.R as SutokoSharedElementsR

/**
 * A preview screen that displays detailed game information
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GamePreview(
    modifier: Modifier = Modifier,
    viewModel: GamePreviewViewModel,
    fallbackBackgroundPainter: Painter? = null,
    onNavigateToGame: (String, Int?, Boolean, String?, Boolean) -> Unit = { _, _, _, _, _ -> },
    onNavigateToChapters: (String) -> Unit = {},
    onOpenAccountConnection: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenOptions: (String) -> Unit = {},
) {
    // Get the game from the ViewModel
    val state by viewModel.game.collectAsStateWithLifecycle()
    val gameItem: GameItem? = (state as? GamePreviewUiState.Data)?.item

    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val isLoadingChapters by viewModel.isLoadingChapters.collectAsStateWithLifecycle()
    val isUserPremium by viewModel.isUserPremium.collectAsStateWithLifecycle()
    val isUserConnected by viewModel.isUserConnected.collectAsStateWithLifecycle()
    val isOptionsVisible by viewModel.isOptionsVisible.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isPreviewVisible by viewModel.isPreviewVisible.collectAsStateWithLifecycle()
    val isMenuSoundMuted by viewModel.isMenuSoundMuted.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()
    val isPurchaseLoading by viewModel.isPurchaseLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val releasedChaptersCount by viewModel.releasedChaptersCount.collectAsStateWithLifecycle()

    val transitionAlpha = remember { Animatable(0f) }
    var isFadingToGame by remember { mutableStateOf(false) }
    var pendingPlay by remember { mutableStateOf<GamePreviewEvent.PlayGame?>(null) }
    var navigationDispatched by remember { mutableStateOf(false) }
    var showAuthorAvatar by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            // Friendzoned games may have advanced their own progress while
            // this screen sat in the back stack: refresh the current chapter.
            viewModel.onResume()
        }
    }

    LaunchedEffect(lifecycleState, pendingPlay) {
        if (lifecycleState != Lifecycle.State.RESUMED) {
            return@LaunchedEffect
        }
        if (navigationDispatched) {
            transitionAlpha.animateTo(0f, tween(250, easing = FastOutSlowInEasing))
            pendingPlay = null
            navigationDispatched = false
            isFadingToGame = false
            return@LaunchedEffect
        }
        val event = pendingPlay ?: return@LaunchedEffect
        isFadingToGame = true
        transitionAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        )
        navigationDispatched = true
        onNavigateToGame(
            event.gameId, event.legacyId, event.isPurchased, event.chapterCode, event.isTrial,
        )
    }

    BackHandler(enabled = isFadingToGame) {}

    Surface(
        modifier = modifier
            .fillMaxSize(),
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { if (!isFadingToGame) viewModel.refresh() },
            modifier = Modifier.fillMaxSize().then(
                if (isFadingToGame) Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    }
                } else Modifier
            ).then(if (isFadingToGame) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Media owns playback eligibility and preserves its last frame during navigation.
                when (val currentState = state) {
                    is GamePreviewUiState.Data -> {
                        GameBackgroundPreviewMedia(
                            imageUrl = currentState.item.menuBackgroundUrl?.takeIf { it.isNotBlank() },
                            videoUrl = currentState.item.videoUrl,
                            fallbackPainter = fallbackBackgroundPainter,
                            modifier = Modifier.fillMaxSize()
                        )
                        GamePreviewMenuSoundEffect(
                            soundUrl = currentState.item.menuSoundUrl,
                            muted = isMenuSoundMuted,
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
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.game_presentation_story_unavailable),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp),
                            )
                            GamePreviewButton(
                                title = stringResource(R.string.game_presentation_game_preview_retry),
                                onClick = viewModel::refresh,
                                modifier = Modifier.padding(horizontal = 32.dp),
                            )
                        }
                    }

                    is GamePreviewUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.game_presentation_game_preview_load_error),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                            GamePreviewButton(
                                title = stringResource(R.string.game_presentation_game_preview_retry),
                                onClick = viewModel::refresh,
                            )
                        }
                    }
                }

                // Get screen dimensions to make translation values adaptable
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp
                val screenHeight = configuration.screenHeightDp

                if (gameItem != null) {
                    GamePreviewGradients(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }

                val unlockFeedbackPending by viewModel.unlockFeedbackPending.collectAsStateWithLifecycle()
                val unlockAnimationIsVisible = unlockFeedbackPending &&
                    lifecycleState == Lifecycle.State.RESUMED && !isFadingToGame
                var showRestartDialog by remember { mutableStateOf(false) }
                var showAlreadyBoughtDialog by remember { mutableStateOf(false) }
                // Non-null => the nickname dialog is visible; the Boolean carries the trial
                // intent (OnTry vs OnPlay) so it is echoed back to the VM on confirm.
                var nickNameDialogIsTrial by remember { mutableStateOf<Boolean?>(null) }
                val context = LocalContext.current
                val haptic = LocalHapticFeedback.current

                var wasDownloading by remember { mutableStateOf(false) }
                val downloadState = gameItem?.downloadState
                LaunchedEffect(downloadState) {
                    if (downloadState is GameDownloadState.Completed && wasDownloading) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    wasDownloading = downloadState is GameDownloadState.Preparing ||
                        downloadState is GameDownloadState.Downloading ||
                        downloadState is GameDownloadState.Installing
                }

                LaunchedEffect(Unit) {
                    viewModel.start()
                }

                LaunchedEffect(unlockAnimationIsVisible) {
                    if (unlockAnimationIsVisible) {
                        delay(3000L)
                        viewModel.onUnlockFeedbackShown()
                    }
                }

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            GamePreviewEvent.PurchaseSuccess -> Unit

                            GamePreviewEvent.OpenAppStore -> {
                                context.openAppInStore()
                            }

                            is GamePreviewEvent.PlayGame -> {
                                nickNameDialogIsTrial = null
                                if (pendingPlay == null && !navigationDispatched) pendingPlay = event
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

                            GamePreviewEvent.OpenShop -> {
                                onOpenShop()
                            }

                            is GamePreviewEvent.ShowError -> Unit
                        }
                    }
                }

                GamePreviewUnlockAnimation(isVisible = unlockAnimationIsVisible)

                val isSavingNickName by viewModel.isSavingNickName.collectAsStateWithLifecycle()
                nickNameDialogIsTrial?.let { isTrial ->
                    NickNameInputDialog(
                        isSaving = isSavingNickName,
                        onConfirm = {
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

                if (gameItem != null) {
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
                                        ?: GamePreviewChapterTitle(text = stringResource(
                                            if (isLoadingChapters) R.string.game_presentation_game_preview_loading_chapter
                                            else R.string.game_presentation_game_preview_choose_chapter
                                        ))

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
                                            if (author.isCertified) {
                                                CertifiedIcon(Color(0xFF2799D7))
                                            }
                                        }
                                    }
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                    isUserConnected = isUserConnected,
                                )

                                Column {
                                    GameActionButtons(
                                        gameActionState = gameActionState,
                                        onAction = viewModel::onAction,
                                        enabled = !isFadingToGame,
                                    )

                                    val chaptersCount = releasedChaptersCount
                                    // Friendzoned games manage their own progress: chapter
                                    // switching from the preview would have no effect on them.
                                    val isFriendzoned =
                                        FriendzonedLegacyIds.isFriendzoned(gameItem?.legacyId)
                                    val showChapters = gameActionState is GameActionState.Play &&
                                        (chaptersCount == null || chaptersCount > 0) && !isFriendzoned
                                    AnimatedVisibility(
                                        visible = showChapters,
                                        enter = fadeIn(tween(180)) + expandVertically(tween(240), expandFrom = Alignment.Top),
                                        exit = fadeOut(tween(120)) + shrinkVertically(tween(240), shrinkTowards = Alignment.Top),
                                    ) {
                                        Column {
                                            Spacer(Modifier.height(12.dp))
                                            GamePreviewButton(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                title = stringResource(R.string.game_presentation_game_story_chapters_button_chapters),
                                                subtitle = chaptersCount?.let { count ->
                                                    stringResource(
                                                        R.string.game_presentation_game_story_chapters_button_chapters_count,
                                                        count,
                                                    )
                                                },
                                                onClick = {
                                                    if (showChapters && lifecycleState == Lifecycle.State.RESUMED) {
                                                        onNavigateToChapters(gameItem.id)
                                                    }
                                                },
                                                isEnabled = showChapters && !isFadingToGame,
                                                icon = Image(
                                                    drawableId = SutokoSharedElementsR.drawable.shared_elements_shared_ic_arrow_back_ios,
                                                    scaleX = -1f,
                                                ),
                                                background = Background.Solid(Color.White.copy(alpha = 0.12f)),
                                            )
                                        }
                                    }

                                    // Admin-only: downloads the preview archive (all chapters
                                    // incl. unreleased). Hidden permanently on any failure.
                                    // Friendzoned games have no preview archive: never show it.
                                    if (isPreviewVisible && !isFriendzoned) {
                                        Spacer(Modifier.height(12.dp))
                                        GamePreviewButton(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            title = stringResource(R.string.game_presentation_game_preview_download_preview),
                                            onClick = {
                                                viewModel.onAction(GamePreviewAction.OnDownloadPreview)
                                            },
                                            background = Background.Solid(Color.White.copy(alpha = 0.12f)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = 10.dp, start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!isUserConnected) {
                        GamePreviewConnectButton(
                            onClick = onOpenAccountConnection,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    gameItem?.let { game ->
                        if (isAdmin) {
                            GamePreviewVersionBadges(
                                currentVersion = game.localVersion,
                                availableVersion = game.version,
                            )
                        }
                    }
                }
                gameItem?.let { game ->
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!game.menuSoundUrl.isNullOrBlank()) {
                            GamePreviewSoundButton(
                                isMuted = isMenuSoundMuted,
                                onToggle = { viewModel.onAction(GamePreviewAction.OnToggleMenuSound) },
                            )
                        }
                        GamePreviewShareButton(
                            onShare = { shareGame(context, game) },
                        )
                        GamePreviewFavoriteButton(
                            isFavorite = game.isFavorite,
                            onToggle = { viewModel.onAction(GamePreviewAction.OnToggleFavorite) },
                        )
                        if (isOptionsVisible) {
                            GamePreviewOptionsButton(
                                onClick = {
                                    if (lifecycleState == Lifecycle.State.RESUMED) onOpenOptions(game.id)
                                },
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

                if (isFadingToGame) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = transitionAlpha.value }
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
