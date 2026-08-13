package com.purpletear.game.data.repository

import android.util.Log
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.local.entity.toDomain
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.toDomain
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_GAMES_PAGE_SIZE = 20
private const val STATUS_ONLINE = "online"
private const val TAG = "GameRepositoryImpl"

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val api: GameApi,
    private val dao: GameDao,
    private val userRepository: UserRepository,
) : GameRepository {

    private data class UserGamesPagination(
        val currentPage: Int = 0,
        val hasMore: Boolean = true
    )

    private var userGamesPagination = UserGamesPagination()


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

    /**
     * Optional bearer token of the logged-in user. Admins get extra content
     * (unreleased stories/chapters) server-side; anonymous players are
     * unaffected (null header is omitted by Retrofit).
     */
    private suspend fun bearerToken(): String? =
        userRepository.observeUser().firstOrNull()?.token?.let { "Bearer $it" }

    override suspend fun getDownloadLink(
        gameId: String,
        userId: String?,
        userToken: String?,
        preview: Boolean,
    ): Result<String> {
        return try {
            val response = api.getDownloadLink(
                gameId = gameId,
                userId = userId,
                userToken = userToken,
                preview = if (preview) true else null,
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
            val remote = api.getOfficialGames(languageTag, authorization = bearerToken())
            val ordered = remote.mapIndexed { index, dto ->
                dto.toDomain().copy(officialOrder = index)
            }
            dao.replaceAllOfficial(ordered)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUserGames(languageTag: String): Result<Unit> {
        return try {
            userGamesPagination = UserGamesPagination()
            val (items, hasMore) = fetchUserGamesPage(languageTag, page = 1)
            dao.replaceAllUserGames(
                items.map { it.toDomain().copy(isOfficial = false) }
            )
            userGamesPagination = userGamesPagination.copy(
                currentPage = 1,
                hasMore = hasMore
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadMoreUserGames(languageTag: String): Result<Boolean> {
        return try {
            val nextPage = userGamesPagination.currentPage + 1
            val (items, hasMore) = fetchUserGamesPage(languageTag, nextPage)
            dao.upsertAll(
                items.map { it.toDomain().copy(isOfficial = false) }
            )
            userGamesPagination = userGamesPagination.copy(
                currentPage = nextPage,
                hasMore = hasMore
            )
            Result.success(hasMore)
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

            val entities = response.body().orEmpty().map {
                it.toDomain()
            }
            upsertPreservingOfficialOrder(entities)
            Result.success(entities.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGameCatalog(id: String, languageTag: String): Result<GameCatalog?> {
        assert(id.isNotBlank())
        return try {
            dao.observeGame(id).first()?.let { entity ->
                return Result.success(entity.toDomain())
            }
            fetchRemoteCatalog(id, languageTag)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshGameCatalog(id: String, languageTag: String): Result<GameCatalog?> {
        assert(id.isNotBlank())
        return fetchRemoteCatalog(id, languageTag)
    }

    /**
     * Remote fetch + persist shared by [getGameCatalog] and [refreshGameCatalog].
     * A found story is upserted so Room observers re-emit; 404 and errors leave
     * the local row untouched.
     */
    private suspend fun fetchRemoteCatalog(id: String, languageTag: String): Result<GameCatalog?> {
        return try {
            val response = api.getStory(
                gameId = id,
                languageCode = languageTag,
                authorization = bearerToken(),
            )
            if (response.code() == 404) {
                return Result.success(null)
            }
            if (!response.isSuccessful) {
                return Result.failure(HttpException(response))
            }

            val entity = response.body()?.toDomain() ?: return Result.success(null)
            upsertPreservingOfficialOrder(listOf(entity))
            Result.success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upserts fetched stories without clobbering the official catalog ordering:
     * syncOfficialGames is the only writer of officialOrder, and an official
     * story must not be demoted by endpoints that omit the `official` flag
     * (search and single-story payloads are partial).
     */
    private suspend fun upsertPreservingOfficialOrder(entities: List<GameCatalogEntity>) {
        if (entities.isEmpty()) return
        val existing = dao.getByIds(entities.map { it.id }).associateBy { it.id }
        dao.upsertAll(entities.map { entity ->
            val current = existing[entity.id] ?: return@map entity
            entity.copy(
                isOfficial = current.isOfficial || entity.isOfficial,
                officialOrder = current.officialOrder,
            )
        })
    }

    override suspend fun getOneUserGames(
        userId: String,
        page: Int,
        limit: Int,
    ): Result<List<GameCatalog>> {
        return try {
            val response = api.getOneUserGames(
                userId = userId,
                page = page,
                limit = limit,
            )

            if (!response.isSuccessful) {
                return Result.failure(HttpException(response))
            }

            // Only this endpoint returns the author's own stories, including offline ones.
            // Map stories one by one: a single malformed payload must not kill the whole list.
            val catalogs = response.body().orEmpty().mapNotNull { dto ->
                runCatching {
                    dto.toDomain().toDomain().copy(
                        isOnline = STATUS_ONLINE.equals(dto.status, ignoreCase = true)
                    )
                }.onFailure { error ->
                    Log.w(TAG, "Skipping malformed story id=${dto.id}", error)
                }.getOrNull()
            }
            Result.success(catalogs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchUserGamesPage(
        languageTag: String,
        page: Int
    ): Pair<List<GameDto>, Boolean> {
        val response: Response<List<GameDto>> = api.getUserGames(
            languageCode = languageTag,
            page = page,
            limit = USER_GAMES_PAGE_SIZE,
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val items = response.body().orEmpty()
        val hasMore = items.size >= USER_GAMES_PAGE_SIZE
        return Pair(items, hasMore)
    }
}
