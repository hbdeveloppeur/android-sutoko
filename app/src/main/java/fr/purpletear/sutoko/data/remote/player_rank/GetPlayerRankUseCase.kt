package fr.purpletear.sutoko.data.remote.player_rank

import com.purpletear.core.presentation.extensions.Resource
import fr.purpletear.sutoko.custom.PlayerRankInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPlayerRankUseCase @Inject constructor(
    private val playerRankRepository: PlayerRankRepository
) {
    fun getTop100PlayerRanks(): Flow<Resource<List<PlayerRankInfo>>> = flow {
        try {
            emit(Resource.Loading())
            delay(1280)
            val result = playerRankRepository.getTop100PlayerRanks()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    //
    fun getTop4PlayersRanks(): Flow<Resource<List<PlayerRankInfo>>> = flow {
        try {
            emit(Resource.Loading())
            delay(1280)
            val result = playerRankRepository.getTop4PlayerRanks()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    fun searchPlayerRank(keyword: String): Flow<Resource<List<PlayerRankInfo>>> = flow {
        try {
            emit(Resource.Loading())
            delay(1280)
            val result = playerRankRepository.searchPlayerRank(keyword)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }
}