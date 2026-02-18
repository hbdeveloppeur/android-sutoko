package fr.purpletear.sutoko.data.remote.player_rank

import fr.purpletear.sutoko.custom.PlayerRankInfo
import javax.inject.Inject

class PlayerRankRepositoryImpl @Inject constructor(
    private val playerRankApi: PlayerRankApi
) : PlayerRankRepository {
    override suspend fun getTop100PlayerRanks(): List<PlayerRankInfo>  {
        return playerRankApi.getTop100PlayerRanks()
    }

    override suspend fun getTop4PlayerRanks(): List<PlayerRankInfo>  {
        return playerRankApi.getTop4PlayerRanks()
    }

    override suspend fun searchPlayerRank(keyword: String): List<PlayerRankInfo> {
        return playerRankApi.searchPlayerRank(keyword)
    }
}