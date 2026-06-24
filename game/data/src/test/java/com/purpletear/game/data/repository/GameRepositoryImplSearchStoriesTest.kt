package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.dto.AssetDto
import com.purpletear.game.data.remote.dto.AuthorDto
import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.GameMetadataDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.HttpException

class GameRepositoryImplSearchStoriesTest {

    private val stubGameDao = object : GameDao {
        override fun observeOfficialGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
        override fun observeUserGames(): Flow<List<GameCatalogEntity>> = flowOf(emptyList())
        override fun observeGame(id: String): Flow<GameCatalogEntity?> = flowOf(null)
        override suspend fun deleteAllOfficial() {}
        override suspend fun deleteAllUserGames() {}
        override suspend fun upsertAll(entities: List<GameCatalogEntity>) {}
    }

    @Test
    fun `searchStories returns mapped domain catalogs on success`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(
                listOf(
                    GameDto(
                        id = "game-1",
                        version = 1,
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
                        logoAsset = null,
                        metadata = GameMetadataDto(
                            title = "Search Result",
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
                        minAppBuild = 1
                    )
                )
            )
        }

        val repository = GameRepositoryImpl(api, stubGameDao)

        val result = repository.searchStories(
            query = "search",
            languageTag = "fr-FR"
        )

        assertTrue("Expected success but got $result", result.isSuccess)
        val catalogs = result.getOrThrow()
        assertEquals(1, catalogs.size)
        assertEquals("game-1", catalogs.first().id)
        assertEquals("Search Result", catalogs.first().metadata.title)
        assertEquals("Author", catalogs.first().author?.displayName)
    }

    @Test
    fun `searchStories returns failure with HttpException on error response`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.error(
                400,
                "Bad Request".toResponseBody(null)
            )
        }

        val repository = GameRepositoryImpl(api, stubGameDao)

        val result = repository.searchStories(
            query = "a",
            languageTag = "fr-FR"
        )

        assertTrue("Expected failure but got $result", result.isFailure)
        assertTrue(
            "Expected HttpException but got ${result.exceptionOrNull()}",
            result.exceptionOrNull() is HttpException
        )
    }

    @Test
    fun `searchStories returns empty list when body is null`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(null)
        }

        val repository = GameRepositoryImpl(api, stubGameDao)

        val result = repository.searchStories(
            query = "query",
            languageTag = "fr-FR"
        )

        assertTrue(result.isSuccess)
        assertEquals(emptyList<GameDto>(), result.getOrThrow())
    }

    private open class FakeGameApi : GameApi {
        override suspend fun getOfficialGames(languageCode: String): List<GameDto> =
            throw NotImplementedError()

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
