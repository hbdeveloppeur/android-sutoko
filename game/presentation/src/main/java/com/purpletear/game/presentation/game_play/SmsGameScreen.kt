package com.purpletear.game.presentation.game_play

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.game_play.components.choices_box.ChoicesBox
import com.purpletear.game.presentation.game_play.components.choices_box.MakeAChoiceButton
import com.purpletear.game.presentation.game_play.components.image_viewer.ImageViewerOverlay
import com.purpletear.game.presentation.game_play.mapper.Message
import com.purpletear.game.presentation.game_play.mapper.characterId
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.HandlerEffect
import kotlinx.coroutines.launch

private data class ImageViewerState(
    val url: String = "",
    val bounds: Rect? = null,
    val isExpanded: Boolean = false,
)

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
    onNextChapterClick: () -> Unit = {},
    onVocalClick: (String) -> Unit = {},
    onChoiceSelected: (HandlerEffect.ShowChoices.Choice) -> Unit = {},
    onRevealChoicesClicked: () -> Unit = {},
    onHideChoicesClicked: () -> Unit = {},
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
                            viewerState = ImageViewerState(url, bounds, true)
                        },
                        onNextChapterClick = handleNextChapterClick,
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

        AnimatedVisibility(
            visible = state.isChoicesRevealed && state.choices.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(durationMillis = CHOICE_FADE_DURATION_MS)),
            exit = fadeOut(animationSpec = tween(durationMillis = CHOICE_FADE_DURATION_MS))
        ) {
            ChoicesBox(
                choices = state.choices,
                onClickChoice = onChoiceSelected,
                onClickClose = onHideChoicesClicked
            )
        }

        ImageViewerOverlay(
            imageUrl = viewerState.url,
            sourceBounds = viewerState.bounds,
            isVisible = viewerState.isExpanded,
            onDismiss = { viewerState = viewerState.copy(isExpanded = false) }
        )

        if (overlayAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(overlayAlpha.value)
                    .background(Color.Black)
            )
        }
    }
}

private const val CHOICE_FADE_DURATION_MS = 320

@Composable
private fun Screen(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}