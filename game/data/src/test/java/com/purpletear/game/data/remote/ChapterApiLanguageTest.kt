package com.purpletear.game.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChapterApiLanguageTest {
    @Test
    fun chapterListUsesTheBackendLanguageCodeQuery() = runBlocking {
        var requestedUrl: HttpUrl? = null
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            requestedUrl = chain.request().url
            okhttp3.Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("[]".toResponseBody())
                .build()
        }.build()
        val api = Retrofit.Builder()
            .baseUrl("https://sutoko.com/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChapterApi::class.java)

        for (languageCode in listOf("es-ES", "es-419")) {
            api.getChapters("gbfCFeRBfmF", languageCode)

            val url = requireNotNull(requestedUrl)
            assertEquals("/api/story/gbfCFeRBfmF/chapters", url.encodedPath)
            assertEquals(languageCode, url.queryParameter("languageCode"))
            assertNull(url.queryParameter("langCode"))
        }
    }
}
