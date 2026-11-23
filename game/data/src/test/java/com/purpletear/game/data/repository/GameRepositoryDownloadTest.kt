package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.GameApi
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GameRepositoryDownloadTest {
    private val requests = mutableListOf<Request>()

    private fun repository(status: Int = 200): GameRepositoryImpl {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            requests += chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message("Test response")
                .body("""{"link":"https://example.invalid/story.zip"}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        val api = Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GameApi::class.java)
        return GameRepositoryImpl(api, stubGameDao, stubUserRepository, stubAppVersionProvider)
    }

    @Test
    fun `normal download sends user credentials without a preview query`() = runTest {
        val result = repository().getDownloadLink("story", "player", "token")
        assertEquals("https://example.invalid/story.zip", result.getOrThrow())
        val url = requests.single().url
        assertEquals("/api/story/story/download-link", url.encodedPath)
        assertEquals("player", url.queryParameter("userId"))
        assertEquals("token", url.queryParameter("userToken"))
        assertEquals(setOf("userId", "userToken"), url.queryParameterNames)
        assertNull(url.queryParameter("preview"))
    }

    @Test
    fun `anonymous download omits credentials and preview query`() = runTest {
        repository().getDownloadLink("story", null, null).getOrThrow()
        assertTrue(requests.single().url.queryParameterNames.isEmpty())
    }

    @Test
    fun `forbidden normal download preserves error and does not retry`() = runTest {
        val result = repository(status = 403).getDownloadLink("story", "player", "token")
        assertTrue(result.exceptionOrNull() is GameDownloadForbiddenException)
        assertEquals(1, requests.size)
    }
}
