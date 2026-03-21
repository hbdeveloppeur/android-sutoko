package com.purpletear.game.presentation.smsgame.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.smsgame.GameUiState
import kotlinx.coroutines.launch
import com.purpletear.game.presentation.smsgame.components.media.MediaComposable as BackgroundMedia

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
) {
    Screen {
        BackgroundMedia(imageUrl = null)

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        LaunchedEffect(state.messages.size) {
            if (state.messages.isNotEmpty()) {
                scope.launch {
                    listState.animateScrollToItem(state.messages.size - 1)
                }
            }
        }

        SmsColumn(state = listState) {
            items(state.messages, key = { it.id }) { message ->
                // MessageBubble(message = message)
            }
        }
    }
}


@Composable
private fun Screen(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), content = content)
}

@Composable
private fun SmsColumn(
    modifier: Modifier = Modifier,
    state: LazyListState,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        content = content,
    )
}