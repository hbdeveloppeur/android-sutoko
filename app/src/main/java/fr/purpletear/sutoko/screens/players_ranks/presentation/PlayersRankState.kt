package fr.purpletear.sutoko.screens.players_ranks.presentation

import androidx.annotation.Keep
import fr.purpletear.sutoko.custom.PlayerRankInfo


@Keep
data class PlayersRankState(
    var initiAuthorsRank : List<PlayerRankInfo> = listOf(),
    var authorsRank : List<PlayerRankInfo> = listOf(),
    var isLoading : Boolean = false
)
