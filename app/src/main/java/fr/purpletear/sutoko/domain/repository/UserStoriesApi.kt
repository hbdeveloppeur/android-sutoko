package fr.purpletear.sutoko.domain.repository

import fr.purpletear.sutoko.helpers.UserStorySearchResult
import retrofit2.http.GET
import retrofit2.http.Path


interface UserStoriesApi {

    @GET("search/story/{keyword}")
    suspend fun getStories(@Path("keyword") keyword: String): List<UserStorySearchResult>
}