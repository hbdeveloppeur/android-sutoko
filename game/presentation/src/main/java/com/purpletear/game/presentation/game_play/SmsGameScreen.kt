package com.purpletear.game.presentation.game_play

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.FakeNotificationOverlay
import com.purpletear.game.presentation.game_play.components.choices_box.ChoicesBox
import com.purpletear.game.presentation.game_play.components.choices_box.MakeAChoiceButton
import com.purpletear.game.presentation.game_play.components.image_viewer.ImageViewerOverlay
import com.purpletear.game.presentation.game_play.components.image_viewer.SwipeToDismissDirection
import com.purpletear.game.presentation.game_play.components.manga.MangaPageScreen
import com.purpletear.game.presentation.game_play.mapper.Message
import com.purpletear.game.presentation.game_play.mapper.characterId
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.message.GameMessageMangaPage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private data class ImageViewerState(
    val imageModel: Any? = null,
    val bounds: Rect? = null,
    val isExpanded: Boolean = false,
    val swipeToDismissDirection: SwipeToDismissDirection = SwipeToDismissDirection.ANY,
)

private data class MangaViewerState(
    val imageUrl: String? = null,
    val overlays: List<GameMessageMangaPage.TextOverlay> = emptyList(),
    val isVisible: Boolean = false,
)

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
    onNextChapterClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onVocalClick: (String) -> Unit = {},
    onChoiceSelected: (HandlerEffect.ShowChoices.Choice) -> Unit = {},
    onRevealChoicesClicked: () -> Unit = {},
    onHideChoicesClicked: () -> Unit = {},
    onMangaPageDismissed: () -> Unit = {},
    onToggleChoicesDarkMode: () -> Unit = {},
    onFakeNotificationDismissed: () -> Unit = {},
    onHoldPauseChanged: (Boolean) -> Unit = {},
    onImageViewerVisibilityChanged: (Boolean) -> Unit = {},
) {
    var viewerState by remember { mutableStateOf(ImageViewerState()) }
    var mangaState by remember { mutableStateOf(MangaViewerState()) }

    Screen(onHoldPauseChanged = onHoldPauseChanged) {
        SceneComposable(
            scene = state.currentScene,
        )

        val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

        val messages = remember(state.messages) { state.messages.asReversed() }


        val newestMessageId = messages.firstOrNull()?.id
        var previousNewestMessageId by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(newestMessageId) {
            previousNewestMessageId = newestMessageId
        }

        var wasScrollable by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(messages.firstOrNull()?.id) {
            if (messages.isEmpty() || listState.isScrollInProgress) return@LaunchedEffect

            withTimeoutOrNull(200) {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                    .first { infos -> infos.any { it.index == 0 && it.size > 0 } }
            }

            val isScrollable = listState.canScrollBackward || listState.canScrollForward
            if (!wasScrollable && isScrollable) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(0)
            }
            wasScrollable = isScrollable
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                itemsIndexed(
                    items = messages,
                    key = { _, item -> item.id }
                ) { index, message ->
                    val characterId = message.characterId()
                    val character = characterId?.let { state.characters[it] }
                    // When the chapter declares a layout, membership in rightSideCharacterIds
                    // is the single source of truth (right if listed, left otherwise).
                    // Otherwise fall back to the legacy main-character rule.
                    val isRightSide = if (state.rightSideCharacterIds.isNotEmpty()) {
                        characterId != null && characterId in state.rightSideCharacterIds
                    } else {
                        character?.isMainCharacter == true
                    }
                    Message(
                        message = message,
                        previousMessage = messages.getOrNull(index + 1),
                        nextMessage = messages.getOrNull(index - 1),
                        character = character,
                        isRightSide = isRightSide,
                        isNewlyAdded = message.id == newestMessageId && message.id != previousNewestMessageId,
                        currentVocalUrl = state.currentVocalUrl,
                        isVocalPlaying = state.isVocalPlaying,
                        vocalProgress = state.vocalProgress,
                        onImageClick = { url, bounds ->
                            onImageViewerVisibilityChanged(true)
                            viewerState =
                                ImageViewerState(url, bounds, true, SwipeToDismissDirection.ANY)
                        },
                        onAvatarClick = { imageModel, bounds ->
                            onImageViewerVisibilityChanged(true)
                            viewerState = ImageViewerState(
                                imageModel,
                                bounds,
                                true,
                                SwipeToDismissDirection.LEFT
                            )
                        },
                        onMangaClick = { url, overlays ->
                            mangaState = MangaViewerState(url, overlays, true)
                        },
                        onNextChapterClick = onNextChapterClick,
                        showNextChapterButton = state.showNextChapterButton,
                        nextChapterTitleRes = state.nextChapterTitleRes,
                        isTrial = state.isTrial,
                        isNextChapterAvailable = state.isNextChapterAvailable,
                        nextChapterReleaseDate = state.nextChapterReleaseDate,
                        gameLogoUrl = state.gameLogoUrl,
                        onBackClick = onBackClick,
                        onVocalClick = onVocalClick,
                    )
                }
            }

            MakeAChoiceButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .alpha(if (state.isAwaitingInput) 1f else 0f),
                onClick = onRevealChoicesClicked,
            )
        }

        AnimatedChoicesBox(
            choices = state.choices,
            visible = state.isChoicesRevealed && state.choices.isNotEmpty(),
            isDarkMode = state.isChoicesDarkMode,
            onClickChoice = onChoiceSelected,
            onClickClose = onHideChoicesClicked,
            onToggleDarkMode = onToggleChoicesDarkMode
        )

        ImageViewerOverlay(
            imageModel = viewerState.imageModel,
            sourceBounds = viewerState.bounds,
            isVisible = viewerState.isExpanded,
            onDismiss = {
                onImageViewerVisibilityChanged(false)
                viewerState = viewerState.copy(isExpanded = false)
            },
            swipeToDismissDirection = viewerState.swipeToDismissDirection,
        )

        MangaPageScreen(
            imageUrl = mangaState.imageUrl,
            overlays = mangaState.overlays,
            isVisible = mangaState.isVisible,
            onDismiss = {
                mangaState = mangaState.copy(isVisible = false)
                onMangaPageDismissed()
            },
        )

        state.fakeNotification?.let { notification ->
            key(notification) {
                FakeNotificationOverlay(
                    notification = notification,
                    onDismissed = onFakeNotificationDismissed,
                )
            }
        }

        HoldPausedIndicator(visible = state.isHoldPaused)

        AnimatedVisibility(
            visible = state.isLoadingStoryUpdates,
            enter = fadeIn(animationSpec = tween(durationMillis = LOADING_FADE_DURATION_MS)),
            exit = fadeOut(animationSpec = tween(durationMillis = LOADING_FADE_DURATION_MS))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 1.dp
                )
            }
        }
    }
}

private const val LOADING_FADE_DURATION_MS = 280
private const val CHOICE_FADE_DURATION_MS = 280

@Composable
private fun AnimatedChoicesBox(
    choices: List<HandlerEffect.ShowChoices.Choice>,
    visible: Boolean,
    isDarkMode: Boolean,
    onClickChoice: (HandlerEffect.ShowChoices.Choice) -> Unit,
    onClickClose: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = CHOICE_FADE_DURATION_MS)),
        exit = fadeOut(animationSpec = tween(durationMillis = CHOICE_FADE_DURATION_MS))
    ) {
        // Capture the choices at entry so the list survives the fade-out even when
        // the ViewModel clears state.choices immediately after a selection.
        var displayedChoices by remember { mutableStateOf(choices) }
        LaunchedEffect(choices) {
            if (choices.isNotEmpty()) {
                displayedChoices = choices
            }
        }

        ChoicesBox(
            choices = displayedChoices,
            isDarkMode = isDarkMode,
            onClickChoice = onClickChoice,
            onClickClose = onClickClose,
            onToggleDarkMode = onToggleDarkMode
        )
    }
}

/**
 * Hold-to-pause: the engine's pacing freezes while a finger rests anywhere on the screen.
 * The detector consumes nothing, so scrolling, message clicks, and choice buttons keep
 * working; any release or cancellation (e.g. a scroll taking over) lifts the pause.
 */
@Composable
private fun Screen(
    onHoldPauseChanged: (Boolean) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onHoldPauseChanged(true)
                    waitForUpOrCancellation()
                    onHoldPauseChanged(false)
                }
            }
    ) {
        content()
    }
}

private const val HOLD_PAUSE_FADE_DURATION_MS = 180

@Composable
private fun BoxScope.HoldPausedIndicator(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 24.dp),
        enter = fadeIn(animationSpec = tween(durationMillis = HOLD_PAUSE_FADE_DURATION_MS)),
        exit = fadeOut(animationSpec = tween(durationMillis = HOLD_PAUSE_FADE_DURATION_MS))
    ) {
        Row(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(percent = 50))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                PauseBar()
                PauseBar()
            }
            Text(
                text = stringResource(R.string.game_presentation_hold_paused),
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PauseBar() {
    Box(
        Modifier
            .width(3.dp)
            .height(11.dp)
            .background(Color.White, RoundedCornerShape(1.dp))
    )
}

