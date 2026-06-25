package com.purpletear.game.data.remote

import com.purpletear.game.data.remote.dto.DownloadLinkResponseDto
import com.purpletear.game.data.remote.dto.GameDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API interface for accessing Game data from the remote server.
 */
interface GameApi {


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
    @GET("portal/stories/search")
    suspend fun searchStories(
        @Query("q") query: String,
        @Query("languageCode") languageCode: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<List<GameDto>>

    /**
     * Get a list of all games.
     *
     * @param languageCode The language code for the games.
     * @return A Response containing a StoriesResponseDto with a list of GameDto objects in the "story" field.
     */
    @GET("portal/stories/official")
    suspend fun getOfficialGames(@Query("languageCode") languageCode: String): List<GameDto>

    @GET("portal/user/{userId}/games")
    suspend fun getOneUserGames(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<List<GameDto>>

    /**
     * Get a paginated list of user-created games.
     *
     * @param languageCode The language code (e.g., "fr-FR")
     * @param page The page number (starting from 1)
     * @param limit The number of items per page
     * @return A Response containing a list of GameDto objects
     */
    @GET("api/games/users")
    suspend fun getUserGames(
        @Query("languageCode") languageCode: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<List<GameDto>>

    /**
     * Get a specific game by its ID.
     *
     * @param storyId The ID of the game to retrieve.
     * @param langCode The language code (e.g., "fr-FR").
     * @return A Response containing the requested GameDto.
     */
    @GET("api/story/{storyId}")
    suspend fun getGame(
        @Path("storyId") storyId: String,
        @Query("langCode") langCode: String,
    ): Response<GameDto>

    /**
     * Generate a download link for a game
     *
     * @param gameId The ID of the game to download
     * @param userId The ID of the user requesting the download
     * @param userToken The token of the user requesting the download
     * @return A Response containing a DownloadLinkResponseDto with the download link in the "link" field
     */
    @GET("api/story/{gameId}/download-link")
    suspend fun getDownloadLink(
        @Path("gameId") gameId: String,
        @Query("userId") userId: String?,
        @Query("userToken") userToken: String?,
    ): DownloadLinkResponseDto


    @POST("api/games/buy")
    suspend fun grantGame(
        @Field("user_id") userId: String,
        @Field("user_token") userToken: String,
        @Field("purchase_token") purchaseToken: String,
        @Field("order_id") orderId: String,
    ): Response<ResponseBody>
}
