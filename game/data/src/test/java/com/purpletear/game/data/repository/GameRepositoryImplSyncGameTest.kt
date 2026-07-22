package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.dto.AuthorDto
import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.GameMetadataDto
import com.purpletear.sutoko.game.model.game.GameMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GameRepositoryImplSyncGameTest {

    @Test
    fun `syncGame fetches the story and upserts it with the new version`() = runTest {
        val dao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getGame(storyId: String, langCode: String): Response<GameDto> {
                assertEquals("game-1", storyId)
                assertEquals("fr-FR", langCode)
                return Response.success(stubGameDto("game-1", version = 7))
            }
        }
        val repository = GameRepositoryImpl(api, dao)

        val result = repository.syncGame("game-1", "fr-FR")

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals(1, dao.upserted.size)
        assertEquals("game-1", dao.upserted.first().id)
        assertEquals(7, dao.upserted.first().version)
    }

    @Test
    fun `syncGame preserves the isOfficial flag of an existing user story`() = runTest {
        val dao = RecordingGameDao(stored = stubEntity("game-1", isOfficial = false))
        val api = object : FakeGameApi() {
            override suspend fun getGame(storyId: String, langCode: String): Response<GameDto> =
                Response.success(stubGameDto("game-1", version = 2, official = true))
        }
        val repository = GameRepositoryImpl(api, dao)

        val result = repository.syncGame("game-1", "fr-FR")

        assertTrue(result.isSuccess)
        assertFalse(dao.upserted.first().isOfficial)
    }

    @Test
    fun `syncGame preserves the isOfficial flag of an existing official story`() = runTest {
        val dao = RecordingGameDao(stored = stubEntity("game-1", isOfficial = true))
        val api = object : FakeGameApi() {
            override suspend fun getGame(storyId: String, langCode: String): Response<GameDto> =
                Response.success(stubGameDto("game-1", version = 2, official = null))
        }
        val repository = GameRepositoryImpl(api, dao)

        val result = repository.syncGame("game-1", "fr-FR")

        assertTrue(result.isSuccess)
        assertTrue(dao.upserted.first().isOfficial)
    }

    @Test
    fun `syncGame returns failure with HttpException on error response`() = runTest {
        val dao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getGame(storyId: String, langCode: String): Response<GameDto> =
                Response.error(404, "Not found".toResponseBody(null))
        }
        val repository = GameRepositoryImpl(api, dao)

        val result = repository.syncGame("game-1", "fr-FR")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
        assertTrue(dao.upserted.isEmpty())
    }

    @Test
    fun `syncGame returns failure when body is null`() = runTest {
        val dao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getGame(storyId: String, langCode: String): Response<GameDto> =
                Response.success(null)
        }
        val repository = GameRepositoryImpl(api, dao)

        val result = repository.syncGame("game-1", "fr-FR")

        assertTrue(result.isFailure)
        assertTrue(dao.upserted.isEmpty())
    }

    private class RecordingGameDao(
        private val stored: GameCatalogEntity? = null,
    ) : GameDao {
        val upserted = mutableListOf<GameCatalogEntity>()

        override fun observeOfficialGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
        override fun observeUserGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
        override fun observeGame(id: String): Flow<GameCatalogEntity?> = flowOf(stored)
        override suspend fun getGame(id: String): GameCatalogEntity? = stored
        override suspend fun deleteAllOfficial() {}
        override suspend fun deleteAllUserGames() {}

        override suspend fun upsertAll(entities: List<GameCatalogEntity>) {
            upserted.addAll(entities)
        }
    }

    private fun stubEntity(id: String, isOfficial: Boolean): GameCatalogEntity = GameCatalogEntity(
        id = id,
        version = 1,
        isOfficial = isOfficial,
        metadata = GameMetadata(title = "Title"),
    )

    private fun stubGameDto(
        id: String,
        version: Int,
        official: Boolean? = null,
    ): GameDto = GameDto(
        id = id,
        version = version,
        interactionCount = 0,
        downloadCount = 0,
        isCertified = false,
        status = "published",
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
        official = official,
        userNickNameRequired = false,
        minAppBuild = 1
    )

    private open class FakeGameApi : GameApi {
        override suspend fun getOfficialGames(languageCode: String): List<GameDto> =
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

        override suspend fun getGame(storyId: String, langCode: String): Response<GameDto> =
            throw NotImplementedError()

        override suspend fun getDownloadLink(
            gameId: String,
            userId: String?,
            userToken: String?
        ) = throw NotImplementedError()

        override suspend fun grantGame(
            userId: String,
            userToken: String,
            purchaseToken: String,
            orderId: String
        ) = throw NotImplementedError()

        override suspend fun searchStories(
            query: String,
            languageCode: String,
            page: Int,
            limit: Int
        ): Response<List<GameDto>> = throw NotImplementedError()
    }
}
