package fr.purpletear.sutoko.screens.main.presentation.screens.calendar

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purpletear.game.presentation.game_catalog.bannerUrl
import com.purpletear.game.presentation.game_catalog.titleUrl
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.game.GameCatalog
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.objects.CalendarEvent
import fr.purpletear.sutoko.objects.nbPlayersToString
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.screens.MainTopNavigationPadding
import fr.purpletear.sutoko.screens.main.presentation.screens.SectionTitle
import fr.purpletear.sutoko.screens.main.presentation.screens.TitleSwitch
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation
import fr.purpletear.sutoko.sync.catalog.CatalogSyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import com.purpletear.game.presentation.R as GamePresentationR

@Composable
internal fun CalendarScreen(
    viewModel: HomeScreenViewModel,
    onSignInPressed: () -> Unit,
    onOptionsPressed: () -> Unit,
    onCoinsPressed: () -> Unit,
    onDiamondsPressed: () -> Unit,
    onGamePressed: (String) -> Unit,
    bottomContentPadding: Dp,
) {
    val state = viewModel.state.value
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val connected by viewModel.isConnected.collectAsStateWithLifecycle()
    val upcomingChapters by viewModel.upcomingCalendarChapters.collectAsStateWithLifecycle()
    val syncStatus by viewModel.catalogSyncStatus.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.refreshCalendar() }
    val releases = remember(state.initialStories, upcomingChapters) {
        val games = state.initialStories.associateBy { it.id }
        upcomingChapters.mapNotNull { chapter ->
            games[chapter.story]?.let { CalendarRelease(it, chapter) }
        }
    }

    CalendarContent(
        events = state.events,
        releases = releases,
        notificationsOn = state.notificationsOn,
        isLoading = syncStatus == CatalogSyncStatus.Loading || viewModel.isCalendarRefreshing.value,
        onNotificationsChanged = { viewModel.onEvent(MainEvents.ToggleNotifications(it)) },
        onGamePressed = onGamePressed,
        bottomContentPadding = bottomContentPadding,
    ) {
        TopNavigation(
            modifier = MainTopNavigationPadding,
            balance = balance,
            isConnected = connected,
            onSignInButtonPressed = onSignInPressed,
            onCoinsButtonPressed = onCoinsPressed,
            onDiamondsButtonPressed = onDiamondsPressed,
            onOptionsButtonPressed = onOptionsPressed,
        )
    }
}

internal data class CalendarRelease(val game: GameCatalog, val chapter: Chapter)

internal const val PureHeartStoryId = "0AZY0NtFQKu"

@Composable
internal fun CalendarContent(
    events: List<CalendarEvent>,
    releases: List<CalendarRelease>,
    notificationsOn: Boolean,
    isLoading: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onGamePressed: (String) -> Unit,
    bottomContentPadding: Dp = 16.dp,
    header: @Composable () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val use24HourTime = DateFormat.is24HourFormat(LocalContext.current)
    val releaseFormatter = remember(locale, use24HourTime) {
        val skeleton = if (use24HourTime) "EEEMMMdHm" else "EEEMMMdhm"
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    }
    val listState = rememberLazyListState()
    val animatedChapter = remember(releases) {
        releases.firstOrNull { it.game.id == PureHeartStoryId }?.chapter
    }
    val animatedCardVisible by remember(listState, animatedChapter) {
        derivedStateOf {
            animatedChapter != null && listState.layoutInfo.visibleItemsInfo.any {
                it.key == "chapter:${animatedChapter.id}"
            }
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        header()
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "schedule") {
                SectionTitle(
                    modifier = Modifier.padding(bottom = 10.dp),
                    title = stringResource(R.string.sutoko_calendar_title),
                )
            }
            item(key = "notifications") {
                TitleSwitch(
                    isOn = notificationsOn,
                    text = stringResource(R.string.notified_when_releases),
                    onCheckedChange = onNotificationsChanged,
                )
            }
            when {
                events.isNotEmpty() -> items(events, key = { "event:${it.id}" }) { event ->
                    val playerLabel = event.nbPlayersToString()?.let {
                        stringResource(R.string.sutoko_event_waited_by, it)
                    }
                    CalendarReleaseCard(
                        title = event.title,
                        subtitle = event.subtitle,
                        imageUrl = event.backgroundImageUrl,
                        secondarySubtitle = event.subSubtitle,
                        playerLabel = playerLabel,
                    )
                }

                releases.isNotEmpty() -> items(releases, key = { "chapter:${it.chapter.id}" }) { release ->
                    val (game, chapter) = release
                    val date = remember(releaseFormatter, chapter.releaseDate) {
                        releaseFormatter.format(Date(chapter.releaseDate * 1000))
                    }
                    CalendarReleaseCard(
                        title = date,
                        subtitle = game.metadata.title,
                        secondarySubtitle = stringResource(
                            GamePresentationR.string.game_presentation_game_chapter_number,
                            chapter.number,
                        ),
                        imageUrl = game.bannerUrl(),
                        titleImageUrl = game.titleUrl(),
                        keywords = game.narrativeThemes.map { it.name },
                        videoRes = R.raw.pure_heart_calendar_loop.takeIf {
                            chapter == animatedChapter && animatedCardVisible
                        },
                        onClick = { onGamePressed(game.id) },
                    )
                }

                isLoading -> item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                else -> item(key = "empty") {
                    SectionTitle(title = stringResource(R.string.sutoko_calendar_empty))
                }
            }
        }
    }
}
