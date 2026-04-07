package com.purpletear.game.presentation.game_play

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.game_play.mapper.Message
import com.purpletear.game.presentation.game_play.state.GameUiState
import kotlinx.coroutines.launch
import com.purpletear.game.presentation.game_play.MediaComposable as BackgroundMedia

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
) {
    Screen {
        BackgroundMedia(imageUrl = null)

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var previousCount by remember { mutableIntStateOf(0) }

        LaunchedEffect(state.messages.size) {
            val currentCount = state.messages.size
            if (currentCount > 0) {
                scope.launch {
                    if (currentCount > previousCount && previousCount > 0) {
                        // New message added: first position at previous last item (no animation),
                        // then animate scroll to new last item for visible push-up effect
                        listState.scrollToItem(previousCount - 1)
                        listState.animateScrollToItem(currentCount - 1)
                    } else {
                        // Initial load or deletion: just animate to last item
                        listState.animateScrollToItem(currentCount - 1)
                    }
                    previousCount = currentCount
                }
            } else {
                previousCount = 0
            }
        }

        SmsColumn(state = listState) {
            items(state.messages, key = { it.id }) { message ->
                Message(
                    message = message,
                    modifier = Modifier.animateContentSize()
                )
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
    var listHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    LazyColumn(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { listHeight = it.height }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        contentPadding = PaddingValues(
            top = with(density) { (listHeight * 0.75f).toDp() },
            bottom = 16.dp
        ),
        content = content,
    )
}
