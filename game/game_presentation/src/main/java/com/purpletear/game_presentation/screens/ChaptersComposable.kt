package com.purpletear.game_presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.purpletear.game_presentation.R
import com.purpletear.game_presentation.components.VerticalGradient
import com.purpletear.game_presentation.model.ChapterState
import com.purpletear.game_presentation.viewmodels.ChaptersViewModel
import kotlinx.coroutines.delay

@Composable
fun ChaptersComposable(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: ChaptersViewModel = hiltViewModel()
) {
    val game by viewModel.game.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1A2E),
                                Color(0xFF16213E)
                            )
                        )
                    )
            )

            VerticalGradient(
                modifier = Modifier.fillMaxSize()
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (chapters.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.game_chapters_no_chapters_available),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    val lazyListState = rememberLazyListState()

                    // Find the index of the current chapter and scroll to it
                    LaunchedEffect(chapters) {
                        val currentChapterIndex =
                            chapters.indexOfFirst { it.state == ChapterState.Current }
                        if (currentChapterIndex != -1) {
                            // Wait for 2 seconds before scrolling
                            delay(280)
                            lazyListState.animateScrollToItem(currentChapterIndex)
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    modifier = Modifier.width(26.dp),
                                    onClick = onNavigateBack
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = stringResource(R.string.game_chapters_back),
                                        tint = Color.White
                                    )
                                }

                                Column(
                                    Modifier
                                        .weight(1f)
                                        .padding(start = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = game?.metadata?.title
                                            ?: stringResource(R.string.game_chapters_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Left
                                    )

                                    Text(
                                        text = stringResource(R.string.game_chapters_select),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray,
                                    )
                                }

                                // Empty spacer to balance the layout
                                Spacer(modifier = Modifier.weight(0.15f))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        items(chapters) { entry ->
                            ChapterItem(
                                chapter = entry.chapter,
                                chapterState = entry.state,
                                onClick = {
                                    viewModel.onClickChapter(entry.chapter)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
