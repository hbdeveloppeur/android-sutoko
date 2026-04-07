package com.purpletear.game.presentation.game_play

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.game_play.mapper.Message
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.game.presentation.game_play.MediaComposable as BackgroundMedia

@Composable
internal fun SmsGameScreen(
    state: GameUiState,
) {
    Screen {
        BackgroundMedia(imageUrl = null)

        val listState = rememberLazyListState()

        SmsColumn(
            state = listState,
            messageCount = state.messages.size
        ) {
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
    messageCount: Int,
    content: LazyListScope.() -> Unit
) {
    var listHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val maxTopPadding = remember(listHeight) {
        with(density) { (listHeight * 0.75f).toDp() }
    }

    // Calculate target top padding: shrinks as more messages are added
    // When screen is full of messages, padding becomes 0
    val targetTopPadding = remember(messageCount, maxTopPadding) {
        if (messageCount <= 1) maxTopPadding else 0.dp
    }

    // Animate the padding change with a long, visible duration
    val animatedTopPadding by animateDpAsState(
        targetValue = targetTopPadding,
        animationSpec = tween(durationMillis = 800),
        label = "top_padding_animation"
    )

    LazyColumn(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { listHeight = it.height }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        contentPadding = PaddingValues(
            top = animatedTopPadding,
            bottom = 16.dp
        ),
        content = content,
    )
}
