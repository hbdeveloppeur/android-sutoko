package com.purpletear.game.data.remote

import com.purpletear.game.data.remote.dto.ChapterDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChapterApi {
    @POST("games/{storyId}/chapters")
    suspend fun getChapters(
        @Path("storyId") storyId: String,
        @Query("langCode") langCode: String,
    ): Response<List<ChapterDto>>

    @GET("chapter/{id}")
    suspend fun getChapter(
        @Path("id") id: Int,
        @Query("langCode") langCode: String,
    ): Response<ChapterDto>
}
