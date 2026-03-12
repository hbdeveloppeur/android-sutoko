package com.purpletear.game.presentation.smsgame.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purpletear.game.presentation.smsgame.GameUiState
import com.purpletear.game.presentation.smsgame.SmsGamePlayViewModel
import com.purpletear.game.presentation.smsgame.engine.MessageItem
import kotlinx.coroutines.launch

@Composable
internal fun SmsGameScreen(
    viewModel: SmsGamePlayViewModel,
    onLoadNextChapter: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0920))
    ) {
        BackgroundMedia(imageUrl = state.backgroundImage)

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error!!)
            state.isCompleted -> CompletedState(
                chapterCode = state.chapterCode ?: "",
                onNextChapter = onLoadNextChapter
            )
            else -> GameContent(
                state = state,
                onChoiceSelected = viewModel::onChoiceSelected
            )
        }
    }
}

@Composable
private fun BackgroundMedia(imageUrl: String?) {
    // TODO: Implement actual background image loading with Coil
    // For now: solid background with slight transparency overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0920))
    ) {
        // Placeholder for background image - remove transparency overlay when implemented
        if (imageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Red,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun CompletedState(
    chapterCode: String,
    onNextChapter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chapter $chapterCode Completed!",
            color = Color.White,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        SimpleButton(
            text = "Continue to Next Chapter",
            onClick = onNextChapter
        )
    }
}

@Composable
private fun GameContent(
    state: GameUiState,
    onChoiceSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }

        AnimatedVisibility(
            visible = state.choices != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ChoicesPanel(
                choices = state.choices ?: emptyList(),
                onChoiceSelected = onChoiceSelected
            )
        }
    }
}

@Composable
private fun MessageBubble(message: MessageItem) {
    val isMainCharacter = message.isMainCharacter
    val backgroundColor = if (isMainCharacter) {
        Color(0xFF6B4EFF)
    } else {
        Color(0xFF2D2D3A)
    }

    val alignment = if (isMainCharacter) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun ChoicesPanel(
    choices: List<String>,
    onChoiceSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0920).copy(alpha = 0.95f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        choices.forEachIndexed { index, choice ->
            ChoiceButton(
                text = choice,
                onClick = { onChoiceSelected(index) }
            )
        }
    }
}

@Composable
private fun ChoiceButton(
    text: String,
    onClick: () -> Unit
) {
    SimpleButton(
        text = text,
        onClick = onClick
    )
}
