package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_GAMES_PAGE_SIZE = 20

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val api: GameApi,
    private val dao: GameDao,
) : GameRepository {


    override fun observeOfficialGames(): Flow<List<GameCatalog>> =
        dao.observeOfficialGames().map { list ->
            list.map {
                it.toDomain()
            }
        }

    override fun observeUserGames(): Flow<List<GameCatalog>> =
        dao.observeUserGames().map { list ->
            list.map {
                it.toDomain()
            }
        }

    override fun observeGame(id: String): Flow<GameCatalog?> {
        assert(id.isNotBlank())
        return dao.observeGame(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getDownloadLink(
        gameId: String,
        userId: String?,
        userToken: String?
    ): Result<String> {
        return try {
            val response = api.getDownloadLink(
                gameId = gameId,
                userId = userId,
                userToken = userToken,
            )
            val url = response.link
            Result.success(url)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 403) {
                Result.failure(GameDownloadForbiddenException(gameId))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun syncOfficialGames(languageTag: String): Result<Unit> {
        return try {
            val remote = api.getOfficialGames(languageTag)
            dao.replaceAllOfficial(remote.map { it.toDomain() })
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUserGames(languageTag: String): Result<Unit> {
        return try {
            val remote = fetchAllUserGames(languageTag)
            dao.replaceAllUserGames(
                remote.map { it.toDomain().copy(isOfficial = false) }
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchStories(
        query: String,
        languageTag: String,
        page: Int,
        limit: Int,
    ): Result<List<GameCatalog>> {
        return try {
            val response = api.searchStories(
                query = query,
                languageCode = languageTag,
                page = page,
                limit = limit,
            )

            if (!response.isSuccessful) {
                return Result.failure(HttpException(response))
            }

            val catalogs = response.body().orEmpty().map {
                it.toDomain().toDomain()
            }
            Result.success(catalogs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchAllUserGames(languageTag: String): List<GameDto> {
        val result = mutableListOf<GameDto>()
        var page = 1

        while (true) {
            val response: Response<List<GameDto>> = api.getUserGames(
                languageCode = languageTag,
                page = page,
                limit = USER_GAMES_PAGE_SIZE,
            )

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val pageItems = response.body().orEmpty()
            if (pageItems.isEmpty()) {
                break
            }

            result.addAll(pageItems)
            page++
        }

        return result
    }
}
