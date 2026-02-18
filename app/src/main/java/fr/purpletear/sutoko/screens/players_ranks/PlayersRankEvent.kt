package fr.purpletear.sutoko.screens.players_ranks

import androidx.annotation.Keep

sealed class PlayersRankEvent {
    @Keep
    data class SearchPlayersRank(val text : String) : PlayersRankEvent()
    @Keep
    data class TextChanged(val text : String) : PlayersRankEvent()
    object ClearSearch : PlayersRankEvent()
}
