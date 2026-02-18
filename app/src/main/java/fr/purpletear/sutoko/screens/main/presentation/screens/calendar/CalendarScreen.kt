package fr.purpletear.sutoko.screens.main.presentation.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.screens.SectionTitle
import fr.purpletear.sutoko.screens.main.presentation.screens.TitleSwitch
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation
import fr.purpletear.sutoko.screens.main.presentation.screens.calendar.components.CalendarEvent


@Composable
fun CalendarScreen(
    viewModel: HomeScreenViewModel
) {
    val state = viewModel.state.value
    LazyColumn(
        modifier = Modifier
            .systemBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 55.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopNavigation(
                coins = -1,
                diamonds = -1,
                isLoading = true,
                onAccountButtonPressed = {
                    viewModel.onEvent(MainEvents.AccountButtonPressed)
                },
                onCoinsButtonPressed = {
                    viewModel.onEvent(MainEvents.CoinButtonPressed)
                },
                onDiamondsButtonPressed = {
                    viewModel.onEvent(MainEvents.DiamondButtonPressed)
                },
                onOptionsButtonPressed = {
                    viewModel.onEvent(MainEvents.OptionButtonPressed)
                }
            )
        }
        item {
            SectionTitle(
                title = stringResource(id = R.string.sutoko_calendar_title),
                subtitle = stringResource(
                    id = R.string.sutoko_calendar_subtitle
                )
            )
        }
        item {
            TitleSwitch(
                isOn = state.notificationsOn,
                text = stringResource(id = R.string.notified_when_releases),
                onCheckedChange = { viewModel.onEvent(MainEvents.ToggleNotifications(it)) }
            )
        }

        items(
            count = state.events.size,
            key = { index -> index },
            itemContent = { index ->
                CalendarEvent(
                    event = state.events[index],
                    onClick = {
                        // todo
                    }
                )
            }
        )
    }
}
