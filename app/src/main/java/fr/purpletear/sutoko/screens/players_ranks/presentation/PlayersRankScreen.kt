package fr.purpletear.sutoko.screens.players_ranks.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.purpletear.sutoko.screens.main.presentation.screens.SectionTitle
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.CommunitySearchBox
import fr.purpletear.sutoko.screens.main.presentation.screens.community.components.MemberCard
import fr.purpletear.sutoko.screens.players_ranks.PlayersRankEvent


@Composable
fun PlayersRankScreen(
    viewModel: PlayersRankViewModel
) {
    val state = viewModel.state.value
    Surface(
        modifier = Modifier
            .background(Color.Gray)
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {


        LazyVerticalGrid(
            modifier = Modifier
                .navigationBarsPadding()
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(
                span = {
                    GridItemSpan(4)
                }) {
                SectionTitle(
                    modifier = Modifier
                        .padding(top = 16.dp),
                    title = "Players Ranks",
                    subtitle = "TOP 200 authors over 50k players"
                )
            }

            item(
                span = {
                    GridItemSpan(4)
                }) {
                CommunitySearchBox(
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                    textChanged = {
                        PlayersRankEvent.TextChanged(it)
                    },
                    onDoneAction = { text ->
                        viewModel.onEvent(PlayersRankEvent.SearchPlayersRank(text))
                    },
                    isLoading = state.isLoading,
                    isClearable = state.authorsRank.size != state.initiAuthorsRank.size,
                    onClear = {
                        viewModel.onEvent(PlayersRankEvent.ClearSearch)
                    }
                )
            }
            items(
                // Counts and key
                count = state.authorsRank.size,
                key = { state.authorsRank[it].id }
            ) { index ->
                MemberCard(playerRank = state.authorsRank[index], color = Color.White)
            }

        }

    }
}