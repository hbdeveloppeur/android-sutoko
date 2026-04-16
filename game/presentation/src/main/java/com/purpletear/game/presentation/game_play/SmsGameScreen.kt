package com.purpletear.game.presentation.game_play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.game_play.components.image_viewer.ImageViewerOverlay
import com.purpletear.game.presentation.game_play.mapper.Message
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.message.GameMessageImage
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping

private data class ImageViewerState(
    val url: String = "",
    val bounds: Rect? = null,
    val isExpanded: Boolean = false,
)

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
) {
    var viewerState by remember { mutableStateOf(ImageViewerState()) }

    Screen {
        SceneComposable(
            scene = state.currentScene,
        )

        val listState = rememberLazyListState()

        val messages = remember(state.messages) {
            state.messages.asReversed()
        }

        val isAtBottom by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset <= 10
            }
        }

        LaunchedEffect(state.messages.lastOrNull()?.id) {
            if (state.messages.isNotEmpty() && isAtBottom) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                val characterId = when (message) {
                    is GameMessageText -> message.characterId
                    is GameMessageTyping -> message.characterId
                    is GameMessageImage -> message.characterId
                    else -> null
                }
                Message(
                    message = message,
                    character = characterId?.let { state.characters[it] },
                    modifier = Modifier.animateItem(),
                    onImageClick = { url, bounds ->
                        viewerState = ImageViewerState(url, bounds, true)
                    }
                )
            }
        }

        ImageViewerOverlay(
            imageUrl = viewerState.url,
            sourceBounds = viewerState.bounds,
            isVisible = viewerState.isExpanded,
            onDismiss = { viewerState = viewerState.copy(isExpanded = false) }
        )
    }
}

@Composable
private fun Screen(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}