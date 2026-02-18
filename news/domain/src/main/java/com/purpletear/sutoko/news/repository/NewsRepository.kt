package com.purpletear.sutoko.news.repository

import com.purpletear.sutoko.news.model.News
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for accessing News data.
 */
interface NewsRepository {
    /**
     * Get a list of all news.
     *
     * @return A Flow emitting a Result containing a list of News.
     */
    fun getNews(): Flow<Result<List<News>>>

    /**
     * Get a specific news by its ID.
     *
     * @param id The ID of the news to retrieve.
     * @return A Flow emitting a Result containing the requested News.
     */
    fun getNewsById(id: Long): Flow<Result<News>>

    /**
     * Refresh the news data from the remote source.
     */
    suspend fun refreshNews()

    /**
     * Observe the cached news data.
     *
     * @return A StateFlow emitting the cached list of News.
     */
    fun observeCachedNews(): StateFlow<List<News>?>
}
