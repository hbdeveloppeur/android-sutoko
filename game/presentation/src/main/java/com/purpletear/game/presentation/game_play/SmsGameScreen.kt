package com.purpletear.game.presentation.game_play

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.components.choices_box.ChoicesBox
import com.purpletear.game.presentation.game_play.components.choices_box.MakeAChoiceButton
import com.purpletear.game.presentation.game_play.components.image_viewer.ImageViewerOverlay
import com.purpletear.game.presentation.game_play.components.image_viewer.SwipeToDismissDirection
import com.purpletear.game.presentation.game_play.mapper.Message
import com.purpletear.game.presentation.game_play.mapper.characterId
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.game.presentation.game_play.state.LiveUpdateStatus
import com.purpletear.sutoko.game.engine.HandlerEffect
import kotlinx.coroutines.launch

private data class ImageViewerState(
    val imageModel: Any? = null,
    val bounds: Rect? = null,
    val isExpanded: Boolean = false,
    val swipeToDismissDirection: SwipeToDismissDirection = SwipeToDismissDirection.ANY,
)

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
    onNextChapterClick: () -> Unit = {},
    onVocalClick: (String) -> Unit = {},
    onChoiceSelected: (HandlerEffect.ShowChoices.Choice) -> Unit = {},
    onRevealChoicesClicked: () -> Unit = {},
    onHideChoicesClicked: () -> Unit = {},
    onReloadStoryUpdates: () -> Unit = {},
) {
    var viewerState by remember { mutableStateOf(ImageViewerState()) }
    val scope = rememberCoroutineScope()
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        overlayAlpha.animateTo(0f, tween(1000))
    }

    val handleNextChapterClick = remember(onNextChapterClick) {
        {
            scope.launch {
                overlayAlpha.animateTo(1f, tween(600))
                onNextChapterClick()
            }
            Unit
        }
    }

    Screen {
        SceneComposable(
            scene = state.currentScene,
        )

        val listState = rememberLazyListState()

        val messages = state.messages.asReversed()

        val isAtBottom by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset <= 10
            }
        }

        // Capture before the new item is laid out; otherwise firstVisibleItemIndex
        // would already have shifted to 1 and we would skip the scroll.
        val shouldAutoScroll = isAtBottom
        LaunchedEffect(messages.firstOrNull()?.id) {
            if (messages.isNotEmpty() && shouldAutoScroll) {
                listState.animateScrollToItem(0)
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            state.liveUpdateStatus?.let { status ->
                LiveUpdateLabel(status = status)
            }

            if (state.hasPendingStoryUpdate) {
                StoryUpdateBanner(onClick = onReloadStoryUpdates)
            }

            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                itemsIndexed(
                    items = messages,
                    key = { _, item -> item.id }
                ) { index, message ->
                    val characterId = message.characterId()
                    Message(
                        message = message,
                        previousMessage = messages.getOrNull(index + 1),
                        character = characterId?.let { state.characters[it] },
                        modifier = Modifier.animateItem(),
                        currentVocalUrl = state.currentVocalUrl,
                        isVocalPlaying = state.isVocalPlaying,
                        vocalProgress = state.vocalProgress,
                        onImageClick = { url, bounds ->
                            viewerState = ImageViewerState(url, bounds, true, SwipeToDismissDirection.ANY)
                        },
                        onAvatarClick = { imageModel, bounds ->
                            viewerState = ImageViewerState(imageModel, bounds, true, SwipeToDismissDirection.LEFT)
                        },
                        onNextChapterClick = handleNextChapterClick,
                        showNextChapterButton = state.showNextChapterButton,
                        nextChapterTitleRes = state.nextChapterTitleRes,
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
            onClickChoice = onChoiceSelected,
            onClickClose = onHideChoicesClicked
        )

        ImageViewerOverlay(
            imageModel = viewerState.imageModel,
            sourceBounds = viewerState.bounds,
            isVisible = viewerState.isExpanded,
            onDismiss = { viewerState = viewerState.copy(isExpanded = false) },
            swipeToDismissDirection = viewerState.swipeToDismissDirection,
        )

        if (overlayAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(overlayAlpha.value)
                    .background(Color.Black)
            )
        }

        if (state.isLoadingStoryUpdates) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White
                )
            }
        }
    }
}

private const val CHOICE_FADE_DURATION_MS = 320

@Composable
private fun AnimatedChoicesBox(
    choices: List<HandlerEffect.ShowChoices.Choice>,
    visible: Boolean,
    onClickChoice: (HandlerEffect.ShowChoices.Choice) -> Unit,
    onClickClose: () -> Unit,
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
            onClickChoice = onClickChoice,
            onClickClose = onClickClose
        )
    }
}

@Composable
private fun Screen(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}


@Composable
private fun LiveUpdateLabel(status: LiveUpdateStatus) {
    val backgroundColor = Color.Black.copy(alpha = 0.6f)
    val (indicatorColor, text) = when (status) {
        LiveUpdateStatus.Connected -> Color(0xFF4CAF50) to stringResource(R.string.live_update_connected)
        LiveUpdateStatus.Disconnected -> Color(0xFFFF9800) to stringResource(R.string.live_update_disconnected)
        LiveUpdateStatus.Loading -> Color.White to stringResource(R.string.live_update_loading)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .height(28.dp)
                .background(backgroundColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (status) {
                    LiveUpdateStatus.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    else -> Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(indicatorColor, RoundedCornerShape(4.dp))
                    )
                }

                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun StoryUpdateBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .background(Color(0xFF2196F3), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.live_update_reload),
                color = Color.White,
                style = MaterialTheme.typography.caption
            )
        }
    }
}
