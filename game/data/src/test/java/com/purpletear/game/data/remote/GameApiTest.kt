package com.purpletear.game.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GameApiTest {

    private val api: GameApi = Retrofit.Builder()
        .baseUrl("https://sutoko.com/")
        .client(OkHttpClient.Builder().cache(null).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GameApi::class.java)

    @Test
    fun `getOfficialGames hits real endpoint and returns success`() = runTest {
        val response = api.getOfficialGames(languageCode = "fr-FR")

        assertTrue(
            "Expected success but got ${response.code()}: ${response.errorBody()?.string()}",
            response.isSuccessful
        )

        val body = response.body()
        assertNotNull("Response body should not be null", body)

        println("Games count: ${body!!.size}")
        body.forEachIndexed { index, game ->
            println("[$index] id=${game.id}, title=${game.metadata.title}, version=${game.version}")
        }

        assertTrue(
            "Expected body to not be empty",
            body.isNotEmpty()
        )
    }

    @Test
    fun `generateGameDownloadLink hits real endpoint and returns success for free game`() = runTest {
        val response = api.generateGameDownloadLink(
            gameId = "KwSEkz9VfM5",
            userId = null,
            userToken = null
        )

        assertTrue(
            "Expected success but got ${response.code()}: ${response.errorBody()?.string()}",
            response.isSuccessful
        )

        val body = response.body()
        assertNotNull("Response body should not be null", body)

        val link = body!!.link
        assertNotNull("Download link should not be null", link)
        assertTrue("Download link should not be blank", link.isNotBlank())

        println("Download link for KwSEkz9VfM5: $link")
    }
}
