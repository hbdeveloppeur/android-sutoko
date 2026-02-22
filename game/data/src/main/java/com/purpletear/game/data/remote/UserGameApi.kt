package com.purpletear.game.data.remote

import com.purpletear.game.data.remote.dto.GameDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API interface for accessing User-created games from sutoko.com/api.
 */
interface UserGameApi {

    /**
     * Get a paginated list of user-created games.
     *
     * @param languageCode The language code (e.g., "fr-FR")
     * @param page The page number (starting from 1)
     * @param limit The number of items per page
     * @return A Response containing a list of GameDto objects
     */
    @GET("games/users")
    suspend fun getUsersGames(
        @Query("languageCode") languageCode: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<List<GameDto>>
}
