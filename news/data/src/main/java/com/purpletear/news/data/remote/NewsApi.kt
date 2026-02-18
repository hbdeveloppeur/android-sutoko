package com.purpletear.news.data.remote

import com.purpletear.news.data.remote.dto.NewsDto
import com.purpletear.news.data.remote.dto.NewsRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API interface for accessing News data from the remote server.
 */
interface NewsApi {
    /**
     * Get a list of all news.
     *
     * @param body The request body containing langCode and versionCode.
     * @return A Response containing a list of NewsDto objects.
     */
    @POST("news")
    suspend fun getNews(
        @Body body: NewsRequestDto
    ): Response<List<NewsDto>>

    /**
     * Get a specific news by its ID.
     *
     * @param id The ID of the news to retrieve.
     * @return A Response containing the requested NewsDto.
     */
    @GET("news/{id}")
    suspend fun getNewsById(@Path("id") id: Long): Response<NewsDto>
}

/**
 * Wrapper for NewsApi to make it more testable.
 * This class can be mocked in tests to avoid making actual network calls.
 */
open class NewsApiWrapper(private val api: NewsApi) {
    /**
     * Get a list of all news.
     *
     * @param langCode The language code for the news.
     * @param versionCode The version code of the project.
     * @return A Response containing a list of NewsDto objects.
     */
    open suspend fun getNews(langCode: String, versionCode: Int): Response<List<NewsDto>> {
        return api.getNews(NewsRequestDto(langCode, versionCode))
    }

    /**
     * Get a specific news by its ID.
     *
     * @param id The ID of the news to retrieve.
     * @return A Response containing the requested NewsDto.
     */
    open suspend fun getNewsById(id: Long): Response<NewsDto> {
        return api.getNewsById(id)
    }
}
