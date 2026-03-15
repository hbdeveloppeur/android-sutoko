package com.purpletear.game.presentation.smsgame.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.purpletear.game.presentation.smsgame.GameUiState
import com.purpletear.game.presentation.smsgame.SmsGamePlayViewModel
import com.purpletear.game.presentation.smsgame.SmsGameRoutes
import com.purpletear.sutoko.game.engine.MessageItem
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.game.presentation.BuildConfig
import kotlinx.coroutines.launch


internal fun NavGraphBuilder.gameScreen(
    gameId: String,
    chapter: Chapter,
    onNextChapter: (chapterCode: String) -> Unit,
) = composable(SmsGameRoutes.GAME) {
    val playViewModel: SmsGamePlayViewModel = hiltViewModel()

    LaunchedEffect(gameId, chapter.code) {
        playViewModel.initialize(gameId, chapter.code)
    }

    SmsGameScreen(
        viewModel = playViewModel,
        gameId = gameId,
        chapterCode = chapter.code,
        onLoadNextChapter = {
            // Get the next chapter code from ViewModel state
            val nextChapterCode = playViewModel.uiState.value.nextChapterCode
            if (nextChapterCode != null) {
                Log.d("TEST", "Next chapter code: $nextChapterCode")
                onNextChapter(nextChapterCode)
            }
        }
    )
}


@Composable
internal fun SmsGameScreen(
    viewModel: SmsGamePlayViewModel,
    gameId: String,
    chapterCode: String,
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
                chapterCode = state.chapterCode ?: chapterCode,
                onNextChapter = onLoadNextChapter
            )
            else -> GameContent(
                state = state,
            )
        }

        // Dev-only: Redownload button at the top (always visible even in error state)
        if (BuildConfig.DEBUG) {
            DevRedownloadButton(
                gameId = gameId,
                chapterCode = chapterCode,
                onRedownload = { gid, cc ->
                    viewModel.clearGameDataAndReinitialize(gid, cc)
                }
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
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
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

@Composable
private fun DevRedownloadButton(
    gameId: String,
    chapterCode: String,
    onRedownload: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Button(
            onClick = { onRedownload(gameId, chapterCode) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.8f),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "[DEV] Redownload Game",
                fontSize = 12.sp
            )
        }
    }
}