package com.purpletear.game.presentation.game_chapters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GameBackgroundPreviewMedia
import com.purpletear.game.presentation.game_preview.components.GamePreviewGradients
import com.purpletear.sutoko.alert.presentation.SimpleAlertDialog
import com.purpletear.sutoko.game.model.Chapter
import java.text.DateFormat
import java.util.Date

/** Brand accent, shared with the in-game chapter selection (current chapter highlight). */
private val ChaptersAccent = Color(0xFFFF007A)
private val CardShape = RoundedCornerShape(12.dp)

/** Accent gradient used by the "coming soon" teaser section. */
private val UpcomingGradientColors = listOf(ChaptersAccent, Color(0xFF7A5CFF))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    viewModel: ChaptersViewModel,
    modifier: Modifier = Modifier,
    fallbackBackgroundPainter: Painter? = null,
    onBack: () -> Unit = {},
    onOpenChapter: (chapterCode: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val data = uiState as? ChaptersUiState.Data
    var pendingChapter by remember { mutableStateOf<Chapter?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ChaptersEvent.OpenChapter -> onOpenChapter(event.chapterCode)
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            GameBackgroundPreviewMedia(
                imageUrl = data?.backgroundUrl?.takeIf { it.isNotBlank() },
                videoUrl = null,
                fallbackPainter = fallbackBackgroundPainter,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xBB16171B))
            )

            GamePreviewGradients(
                screenWidth = LocalConfiguration.current.screenWidthDp,
                screenHeight = LocalConfiguration.current.screenHeightDp,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                ChaptersTopBar(onBack = onBack)

                when (val state = uiState) {
                    ChaptersUiState.Loading -> ChaptersCenteredContent {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = ChaptersAccent,
                            strokeWidth = 2.5.dp,
                        )
                    }

                    ChaptersUiState.Error -> ChaptersCenteredContent {
                        ChaptersMessageText(
                            text = stringResource(R.string.game_presentation_error_load_game),
                        )
                    }

                    is ChaptersUiState.Data -> {
                        if (state.chapters.isEmpty()) {
                            ChaptersCenteredContent {
                                ChaptersMessageText(
                                    text = stringResource(R.string.game_presentation_game_chapters_no_chapters_available),
                                )
                            }
                        } else {
                            ChapterList(
                                chapters = state.chapters,
                                currentChapterCode = state.currentChapterCode,
                                onChapterClick = { chapter ->
                                    val isCurrent =
                                        chapter.normalizedCode == state.currentChapterCode
                                    if (isCurrent || state.currentChapterCode == null) {
                                        // Nothing to lose: open directly.
                                        viewModel.onChapterSelected(chapter)
                                    } else {
                                        pendingChapter = chapter
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingChapter?.let { chapter ->
        SimpleAlertDialog(
            onDismissRequest = { pendingChapter = null },
            onConfirmation = {
                pendingChapter = null
                viewModel.onChapterSelected(chapter)
            },
            dialogTitle = stringResource(
                R.string.game_presentation_game_chapters_goto_title,
                chapter.number,
            ),
            dialogText = stringResource(R.string.game_presentation_game_chapters_goto_description),
            confirmButtonText = stringResource(R.string.game_presentation_game_chapters_continue),
            dismissButtonText = stringResource(android.R.string.cancel),
        )
    }
}

@Composable
private fun ChaptersTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.game_presentation_game_chapters_back),
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = stringResource(R.string.game_presentation_game_chapters_title),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
        )
    }
}

@Composable
private fun ChapterList(
    chapters: List<Chapter>,
    currentChapterCode: String?,
    onChapterClick: (Chapter) -> Unit,
) {
    // Locked chapters leave the main list: they are teased in a dedicated top section.
    val (upcoming, released) = remember(chapters) { chapters.partition { !it.isAvailable } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
    ) {
        if (upcoming.isNotEmpty()) {
            item(key = "upcoming-header") {
                UpcomingSectionHeader(
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
                )
            }
            items(
                items = upcoming,
                key = { "upcoming-" + it.id.ifBlank { "chapter-${it.number}" } },
            ) { chapter ->
                UpcomingChapterCard(chapter = chapter)
            }
        }
        item(key = "chapters-header") {
            Text(
                text = stringResource(
                    R.string.game_presentation_game_story_chapters_button_chapters_count,
                    chapters.size,
                ),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontFamily = PlusJakartaSansFontFamily,
                modifier = Modifier
                    .background(Color(0xFF16171B))
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                    .fillMaxWidth(),
            )
        }
        items(
            items = released,
            key = { it.id.ifBlank { "chapter-${it.number}" } },
        ) { chapter ->
            ChapterCard(
                chapter = chapter,
                isCurrent = chapter.normalizedCode == currentChapterCode,
                onClick = { onChapterClick(chapter) },
            )
        }
    }
}

/**
 * Section header for the "coming soon" teasers pinned on top of the list.
 */
@Composable
private fun UpcomingSectionHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.game_presentation_calendar),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.game_presentation_game_chapters_upcoming_title),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
            letterSpacing = 1.sp,
        )
    }
}

/**
 * Teaser card for a chapter releasing in the future: gradient border and number,
 * title, release date, and a lock badge. Never clickable.
 */
@Composable
private fun UpcomingChapterCard(
    chapter: Chapter,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 14.dp, top = 18.dp, bottom = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ChapterNumber(
                number = chapter.number,
                isCurrent = false,
                isAvailable = false,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (chapter.title.isNotBlank()) {
                    Text(
                        text = chapter.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSansFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ChapterAvailabilityDate(releaseDate = chapter.releaseDate)
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.game_presentation_game_chapter_locked),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAvailable = chapter.isAvailable
    val contentAlpha = if (isAvailable) 1f else 0.45f

    // Flat dark card, identity carried by a thin accent bar on the left edge.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF16171B))
            .clickable(enabled = isAvailable, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .matchParentSize()
                .width(3.dp)
                .background(
                    when {
                        isCurrent -> ChaptersAccent
                        isAvailable -> ChaptersAccent.copy(alpha = 0.35f)
                        else -> Color.White.copy(alpha = 0.08f)
                    }
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChapterNumber(
                number = chapter.number,
                isCurrent = isCurrent,
                isAvailable = isAvailable,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha),
                verticalArrangement = spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                ) {
                    if (chapter.title.isNotBlank()) {
                        Text(
                            text = chapter.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSansFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                    if (isCurrent) {
                        ChapterCurrentBadge()
                    }
                }

                if (chapter.description.isNotBlank()) {
                    Text(
                        text = chapter.description,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = PlusJakartaSansFontFamily,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (!isAvailable) {
                    ChapterAvailabilityDate(releaseDate = chapter.releaseDate)
                }
            }

            ChapterTrailingIcon(isAvailable = isAvailable)
        }
    }
}

/**
 * The chapter number is the identity of the row: a large two-digit numeral in
 * the brand accent (dimmed when locked), no tile, no box.
 */
@Composable
private fun ChapterNumber(
    number: Int,
    isCurrent: Boolean,
    isAvailable: Boolean,
) {
    Text(
        text = number.toString().padStart(2, '0'),
        color = when {
            isCurrent -> ChaptersAccent
            isAvailable -> ChaptersAccent.copy(alpha = 0.75f)
            else -> Color.White.copy(alpha = 0.25f)
        },
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PlusJakartaSansFontFamily,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(34.dp),
    )
}

@Composable
private fun ChapterTrailingIcon(isAvailable: Boolean) {
    if (isAvailable) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.55f),
            modifier = Modifier.size(22.dp),
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = stringResource(R.string.game_presentation_game_chapter_locked),
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ChapterCurrentBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = stringResource(R.string.game_presentation_game_chapter_current).uppercase(),
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
private fun ChapterAvailabilityDate(releaseDate: Long) {
    if (releaseDate <= 0L) return
    // releaseDate is epoch seconds (server format).
    val dateText = remember(releaseDate) {
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(releaseDate * 1000))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.game_presentation_calendar),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = stringResource(
                R.string.game_presentation_game_chapter_available_on,
                dateText,
            ),
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PlusJakartaSansFontFamily,
        )
    }
}

@Composable
private fun ChaptersCenteredContent(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ChaptersMessageText(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 15.sp,
        fontFamily = PlusJakartaSansFontFamily,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(24.dp),
    )
}
