package com.purpletear.news.data.repository

import com.purpletear.news.data.remote.NewsApiWrapper
import com.purpletear.news.data.remote.dto.ActionDto
import com.purpletear.news.data.remote.dto.NewsDto
import com.purpletear.news.data.remote.dto.NewsMetadataDto
import com.purpletear.news.data.remote.dto.media.MediaImageDto
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NewsRepositoryImplTest {

    private lateinit var apiWrapper: NewsApiWrapper
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var repository: NewsRepositoryImpl

    @Before
    fun setup() {
        apiWrapper = mockk()
        appVersionProvider = mockk()
        every { appVersionProvider.getVersionCode() } returns 1
        repository = NewsRepositoryImpl(apiWrapper, appVersionProvider)
    }

    @Test
    fun `observeNews emits empty list before any sync`() = runTest {
        assertEquals(emptyList<com.purpletear.sutoko.news.model.News>(), repository.observeNews().first())
    }

    @Test
    fun `syncNews fetches news and observeNews emits cached result`() = runTest {
        val newsDto = createNewsDto(id = 1L)
        coEvery { apiWrapper.getNews(any(), any()) } returns Response.success(listOf(newsDto))

        val result = repository.syncNews("en-US")

        assertTrue(result.isSuccess)
        val observed = repository.observeNews().first()
        assertEquals(1, observed.size)
        assertEquals(1L, observed.first().id)
        coVerify(exactly = 1) { apiWrapper.getNews("en", 1) }
    }

    @Test
    fun `syncNews returns failure when api call is not successful`() = runTest {
        coEvery { apiWrapper.getNews(any(), any()) } returns Response.error(
            500,
            "Server error".toResponseBody(null)
        )

        val result = repository.syncNews("fr-FR")

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { apiWrapper.getNews("fr", 1) }
    }

    @Test
    fun `syncNews returns failure on exception`() = runTest {
        coEvery { apiWrapper.getNews(any(), any()) } throws RuntimeException("Network error")

        val result = repository.syncNews("en-US")

        assertTrue(result.isFailure)
    }

    private fun createNewsDto(id: Long): NewsDto {
        return NewsDto(
            id = id,
            link = null,
            os = "android",
            createdAt = 0L,
            publishDate = 0L,
            releaseDateAndroid = null,
            media = MediaImageDto(
                id = 1L,
                type = "image",
                width = 100,
                height = 100,
                bytes = 1000,
                directory = "/",
                mimeType = "image/png",
                filename = "news.png"
            ),
            untilDate = Long.MAX_VALUE,
            untilVersionExclusive = null,
            metadata = NewsMetadataDto(
                title = "Title",
                subtitle = "Subtitle",
                catchingPhrase = "Catch phrase"
            ),
            action = ActionDto(
                name = "OpenLink",
                value = "https://example.com"
            )
        )
    }
}
