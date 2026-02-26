package com.purpletear.game.data.remote

import com.purpletear.game.data.remote.dto.DownloadLinkResponseDto
import com.purpletear.game.data.remote.dto.GameDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API interface for accessing Game data from the remote server.
 */
interface GameApi {
    /**
     * Get a list of all games.
     *
     * @param langCode The language code for the games.
     * @return A Response containing a StoriesResponseDto with a list of GameDto objects in the "story" field.
     */
    @POST("games")
    suspend fun getOfficialGames(@Query("langCode") langCode: String): Response<List<GameDto>>


    /**
     * Get a paginated list of user-created games.
     *
     * @param languageCode The language code (e.g., "fr-FR")
     * @param page The page number (starting from 1)
     * @param limit The number of items per page
     * @return A Response containing a list of GameDto objects
     */
    @GET("games/users")
    suspend fun getUserGames(
        @Query("languageCode") languageCode: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<List<GameDto>>

    /**
     * Get a specific game by its ID.
     *
     * @param storyId The ID of the game to retrieve.
     * @return A Response containing the requested GameDto.
     */
    @GET("story/{storyId}")
    suspend fun getGame(
        @Path("storyId") storyId: String,
    ): Response<GameDto>

    /**
     * Generate a download link for a game
     *
     * @param gameId The ID of the game to download
     * @param userId The ID of the user requesting the download
     * @param userToken The token of the user requesting the download
     * @return A Response containing a DownloadLinkResponseDto with the download link in the "link" field
     */
    @GET("story/{gameId}/download-link")
    suspend fun generateGameDownloadLink(
        @Path("gameId") gameId: String,
        @Query("userId") userId: String?,
        @Query("userToken") userToken: String?,
    ): Response<DownloadLinkResponseDto>
}
