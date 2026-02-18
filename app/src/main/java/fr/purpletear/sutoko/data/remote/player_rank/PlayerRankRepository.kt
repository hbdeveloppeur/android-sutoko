package fr.purpletear.sutoko.data.remote.player_rank

import fr.purpletear.sutoko.custom.PlayerRankInfo

interface PlayerRankRepository {
    suspend fun getTop100PlayerRanks(): List<PlayerRankInfo>
    suspend fun getTop4PlayerRanks(): List<PlayerRankInfo>
    suspend fun searchPlayerRank(keyword: String): List<PlayerRankInfo>
}