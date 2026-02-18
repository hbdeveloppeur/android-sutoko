package com.purpletear.game_data.remote

import com.purpletear.game_data.remote.dto.DownloadLinkRequestDto
import com.purpletear.game_data.remote.dto.DownloadLinkResponseDto
import com.purpletear.game_data.remote.dto.FreeDownloadLinkRequestDto
import com.purpletear.game_data.remote.dto.GameDto
import retrofit2.Response
import retrofit2.http.Body
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
    suspend fun getGames(@Query("langCode") langCode: String): Response<List<GameDto>>

    /**
     * Get a specific game by its ID.
     *
     * @param storyId The ID of the game to retrieve.
     * @return A Response containing a StoryResponseDto with the requested GameDto in the "story" field.
     */
    @POST("games/{storyId}")
    suspend fun getGame(
        @Path("storyId") storyId: Int,
        @Query("langCode") langCode: String,
    ): Response<GameDto>

    /**
     * Generate a download link for a game
     *
     * @return A Response containing a DownloadLinkResponseDto with the download link in the "link" field
     */
    @POST("game/download-link")
    suspend fun generateGameDownloadLink(
        @Body body: DownloadLinkRequestDto,
    ): Response<DownloadLinkResponseDto>

    /**
     * Generate a download link for a free game
     *
     * @return A Response containing a DownloadLinkResponseDto with the download link in the "link" field
     */
    @POST("game/free-download-link")
    suspend fun generateFreeGameDownloadLink(
        @Body body: FreeDownloadLinkRequestDto,
    ): Response<DownloadLinkResponseDto>
}
