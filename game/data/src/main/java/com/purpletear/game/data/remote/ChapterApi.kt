package com.purpletear.game.data.remote

import com.purpletear.game.data.remote.dto.ChapterDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API interface for accessing Chapter data from the remote server.
 */
interface ChapterApi {
    /**
     * Get a list of chapters for a specific story (game) ID.
     *
     * @param storyId The ID of the story (game) to retrieve chapters for.
     * @return A Response containing a list of ChapterDto objects.
     */
    @POST("games/{storyId}/chapters")
    suspend fun getChapters(
        @Path("storyId") storyId: Int,
        @Query("langCode") langCode: String,
    ): Response<List<ChapterDto>>

    /**
     * Get a specific chapter by its ID.
     *
     * @param id The ID of the chapter to retrieve.
     * @return A Response containing the requested ChapterDto.
     */
    @GET("chapter/{id}")
    suspend fun getChapter(
        @Path("id") id: Int,
        @Query("langCode") langCode: String,
    ): Response<ChapterDto>
}