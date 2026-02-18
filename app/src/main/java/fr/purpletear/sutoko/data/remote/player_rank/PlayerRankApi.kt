package fr.purpletear.sutoko.data.remote.player_rank

import fr.purpletear.sutoko.custom.PlayerRankInfo
import retrofit2.http.GET
import retrofit2.http.Path

interface PlayerRankApi {
    @GET("get/users/top-100")
    suspend fun getTop100PlayerRanks(): List<PlayerRankInfo>

    @GET("get/users/top-4")
    suspend fun getTop4PlayerRanks(): List<PlayerRankInfo>

    @GET("search/user/{keyword}")
    suspend fun searchPlayerRank(@Path("keyword") keyword : String): List<PlayerRankInfo>
}