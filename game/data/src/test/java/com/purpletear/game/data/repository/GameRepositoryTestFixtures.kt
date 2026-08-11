package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.dto.AuthorDto
import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.GameMetadataDto
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

internal val stubUserRepository: UserRepository = object : UserRepository {
    override fun observeUser(): Flow<User?> = flowOf(null)
    override fun observeIsConnected(): Flow<Boolean> = flowOf(false)
    override fun isConnected(): Result<Boolean> = Result.success(false)
    override suspend fun connect(id: String, token: String): Result<Unit> = Result.success(Unit)
    override suspend fun disconnect(): Result<Unit> = Result.success(Unit)
}

internal val stubGameDao: GameDao = object : GameDao {
    override fun observeOfficialGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
    override fun observeUserGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
    override fun observeGame(id: String): Flow<GameCatalogEntity?> = flowOf(null)
    override suspend fun getByIds(ids: List<String>): List<GameCatalogEntity> = emptyList()
    override suspend fun deleteAllOfficial() {}
    override suspend fun deleteAllUserGames() {}
    override suspend fun upsertAll(entities: List<GameCatalogEntity>) {}
}

internal fun gameRepository(
    api: GameApi,
    dao: GameDao = stubGameDao,
    userRepository: UserRepository = stubUserRepository,
): GameRepositoryImpl = GameRepositoryImpl(api, dao, userRepository)

internal class RecordingGameDao : GameDao {
    val replaceAllOfficialCalls = mutableListOf<List<GameCatalogEntity>>()
    val replaceAllUserGamesCalls = mutableListOf<List<GameCatalogEntity>>()
    val upsertAllCalls = mutableListOf<List<GameCatalogEntity>>()
    var game: GameCatalogEntity? = null
    var storedGames: Map<String, GameCatalogEntity> = emptyMap()

    override fun observeOfficialGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
    override fun observeUserGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
    override fun observeGame(id: String): Flow<GameCatalogEntity?> = flowOf(game)

    override suspend fun getByIds(ids: List<String>): List<GameCatalogEntity> =
        ids.mapNotNull { storedGames[it] }

    override suspend fun deleteAllOfficial() {}
    override suspend fun deleteAllUserGames() {}

    override suspend fun upsertAll(entities: List<GameCatalogEntity>) {
        upsertAllCalls.add(entities)
    }

    override suspend fun replaceAllUserGames(entities: List<GameCatalogEntity>) {
        replaceAllUserGamesCalls.add(entities)
    }

    override suspend fun replaceAllOfficial(entities: List<GameCatalogEntity>) {
        replaceAllOfficialCalls.add(entities)
    }
}

internal open class FakeGameApi : GameApi {
    override suspend fun getOfficialGames(
        languageCode: String,
        authorization: String?,
    ): List<GameDto> =
        throw NotImplementedError()

    override suspend fun getOneUserGames(
        userId: String,
        page: Int,
        limit: Int
    ): Response<List<GameDto>> = throw NotImplementedError()

    override suspend fun getUserGames(
        languageCode: String,
        page: Int,
        limit: Int
    ): Response<List<GameDto>> = throw NotImplementedError()

    override suspend fun getDownloadLink(
        gameId: String,
        userId: String?,
        userToken: String?,
        preview: Boolean?,
    ) = throw NotImplementedError()

    override suspend fun searchStories(
        query: String,
        languageCode: String,
        page: Int,
        limit: Int
    ): Response<List<GameDto>> = throw NotImplementedError()

    override suspend fun getStory(
        gameId: String,
        languageCode: String,
        authorization: String?,
    ): Response<GameDto> =
        throw NotImplementedError()
}

internal fun stubGameDto(id: String): GameDto = GameDto(
    id = id,
    version = 1,
    interactionCount = 0,
    downloadCount = 0,
    isCertified = false,
    status = "online",
    createdAt = 0L,
    price = 0,
    skuIdentifiers = emptyList(),
    videoUrl = null,
    cachedChaptersCount = 5,
    bannerAsset = null,
    menuBackgroundAsset = null,
    titleAsset = null,
    logoAsset = null,
    metadata = GameMetadataDto(
        title = "Title",
        description = null,
        lang = "fr-FR",
        catchingPhrase = null
    ),
    author = AuthorDto(
        displayName = "Author",
        avatarUrl = null,
        isCertified = false
    ),
    legacyId = null,
    official = false,
    userNickNameRequired = false,
    canvasTechnologyRequiredVersion = 1
)
