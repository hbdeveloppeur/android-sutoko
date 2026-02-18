package fr.purpletear.sutoko.screens.main.presentation.screens.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.purpletear.core.presentation.extensions.Resource
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.presentation.HomeScreenViewModel
import fr.purpletear.sutoko.screens.main.presentation.MainEvents
import fr.purpletear.sutoko.screens.main.presentation.screens.SectionTitle
import fr.purpletear.sutoko.screens.main.presentation.screens.TopNavigation
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.CommunityCtaRow
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.CommunitySearchBox
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.LoadMoreButton
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.MembersRankShort
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.UserStory

@Composable
fun CommunityScreen(
    viewModel: HomeScreenViewModel
) {
    val focusManager = LocalFocusManager.current
    LazyColumn(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .navigationBarsPadding()
            .systemBarsPadding()
            .padding(bottom = 55.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TopNavigation(
                coins = viewModel.coinsBalance.value.data?.coins ?: -1,
                diamonds = viewModel.coinsBalance.value.data?.diamonds ?: -1,
                isLoading = viewModel.coinsBalance.value is Resource.Loading,
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
            CommunityCtaRow(
                modifier = Modifier
                    .clickable {
                        viewModel.onEvent(MainEvents.TapCreateStory)
                    }
            )
        }

        item {
            SectionTitle(title = stringResource(R.string.sutoko_community_rank_title))
        }

        item {
            MembersRankShort(
                viewModel = viewModel
            )
        }

        item {
            SectionTitle(title = stringResource(R.string.sutoko_community_title))
        }

        item {
            CommunitySearchBox(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                onDoneAction = { text ->
                    viewModel.onEvent(MainEvents.SearchStories(text))
                },
                isLoading = viewModel.state.value.isLoading,
                isClearable = viewModel.state.value.userStoriesSearchIsClearable,
                onClear = {
                    viewModel.onEvent(MainEvents.TapClearUserStoriesSearch)
                }
            )
        }
        items(
            count = viewModel.state.value.userStories.size,
            key = { index ->
                "us_$index"
            }) {
            UserStory(
                story = viewModel.state.value.userStories[it],
                onClick = { story ->
                    viewModel.onEvent(MainEvents.TapStory(story))
                }
            )
        }
        item {
            LoadMoreButton(
                modifier = Modifier
                    .padding(bottom = 12.dp),
                isLoading = viewModel.state.value.isLoadingMoreStories,

                onClick = {
                    viewModel.onEvent(MainEvents.TapLoadMoreStories)
                }
            )
        }
    }
}
