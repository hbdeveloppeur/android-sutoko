package com.purpletear.game.data.remote

import com.purpletear.game.data.remote.dto.GameDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API interface for Game Portal endpoints.
 * Base URL: https://sutoko.com/portal/
 */
interface GamePortalApi {

    /**
     * Search for stories by query string.
     * Searches story titles, author names, and categories.
     *
     * @param query The search query (2-100 characters)
     * @param languageCode The language code (e.g., "fr-FR", "en-US")
     * @param page The page number (starting from 1, default: 1)
     * @param limit The number of items per page (1-20, default: 20)
     * @return A Response containing a list of GameDto objects matching the search criteria
     *
     * Error codes:
     * - 400: Validation error (query too short, etc.)
     * - 404: Language not found
     * - 429: Rate limit exceeded (30 requests per minute per device/IP)
     * - 500: Server error
     */
    @GET("stories/search")
    suspend fun searchStories(
        @Query("q") query: String,
        @Query("languageCode") languageCode: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<List<GameDto>>
}
