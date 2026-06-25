package com.purpletear.sutoko.news.repository

import com.purpletear.sutoko.news.model.News
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing News data.
 */
interface NewsRepository {
    /**
     * Observe the cached list of news.
     *
     * @return A Flow emitting the list of News.
     */
    fun observeNews(): Flow<List<News>>

    /**
     * Get a specific news by its ID.
     *
     * @param id The ID of the news to retrieve.
     * @return A Flow emitting a Result containing the requested News.
     */
    fun getNewsById(id: Long): Flow<Result<News>>

    /**
     * Synchronize news with the remote source.
     *
     * @param languageTag The BCP-47 language tag to request news for.
     * @return A Result indicating success or failure of the sync operation.
     */
    suspend fun syncNews(languageTag: String): Result<Unit>
}
