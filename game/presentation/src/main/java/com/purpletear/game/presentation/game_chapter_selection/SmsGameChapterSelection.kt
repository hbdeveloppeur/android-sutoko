package com.purpletear.game.presentation.game_chapter_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sharedelements.theme.Poppins
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.SmsGameRoutes
import com.purpletear.sutoko.game.model.Chapter

internal fun NavGraphBuilder.chapterSelectionScreen(
    gameId: String,
    onNavigateBack: () -> Unit,
) = composable(
    route = SmsGameRoutes.CHAPTER_SELECTION,
    arguments = listOf(
        navArgument("currentChapterCode") {
            type = NavType.StringType
            defaultValue = ""
        }
    )
) { backStackEntry ->
    val viewModel: SmsGameChapterSelectionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentChapterCode = backStackEntry.arguments?.getString("currentChapterCode") ?: ""

    LaunchedEffect(viewModel, currentChapterCode) {
        viewModel.initialize(gameId, currentChapterCode)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                ChapterSelectionEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    ChapterSelectionRoute(
        uiState = uiState,
        onBackClick = onNavigateBack,
        onChapterClick = { chapter -> viewModel.onChapterSelected(gameId, chapter) },
    )
}

@Composable
private fun ChapterSelectionRoute(
    uiState: ChapterSelectionUiState,
    onBackClick: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBackClick = onBackClick)

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.errorMessage != null -> ErrorMessage(message = uiState.errorMessage)
                else -> ChapterList(
                    chapters = uiState.chapters,
                    currentChapterCode = uiState.currentChapterCode,
                    onChapterClick = onChapterClick,
                )
            }
        }
    }
}

@Composable
private fun TopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.game_presentation_game_chapters_back),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.game_presentation_game_chapter_selection_title),
            color = Color.White,
            fontFamily = Poppins,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ChapterList(
    chapters: List<Chapter>,
    currentChapterCode: String,
    onChapterClick: (Chapter) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        items(
            items = chapters,
            key = { it.id }
        ) { chapter ->
            ChapterItem(
                chapter = chapter,
                isCurrent = chapter.normalizedCode == currentChapterCode.lowercase(),
                onClick = { onChapterClick(chapter) },
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isCurrent) Color(0xFFFF007A) else Color(0xFF1B1D22)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.game_presentation_game_chapter_number, chapter.number),
                color = if (isCurrent) Color.White else Color(0xFF8C8C8C),
                fontFamily = Poppins,
                fontSize = 12.sp,
            )
            Text(
                text = chapter.title,
                color = Color.White,
                fontFamily = Poppins,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (isCurrent) {
            Text(
                text = stringResource(R.string.game_presentation_game_chapter_selection_current),
                color = Color.White,
                fontFamily = Poppins,
                fontSize = 12.sp,
            )
        } else if (!chapter.isAvailable) {
            Text(
                text = stringResource(R.string.game_presentation_game_chapter_selection_locked),
                color = Color(0xFF8C8C8C),
                fontFamily = Poppins,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.LightGray,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ErrorMessage(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message ?: stringResource(R.string.game_presentation_error_load_game),
            color = Color.White,
            fontFamily = Poppins,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}
