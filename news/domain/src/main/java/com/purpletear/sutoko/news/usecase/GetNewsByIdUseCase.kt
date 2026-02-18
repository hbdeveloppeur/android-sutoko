package com.purpletear.sutoko.news.usecase

import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a specific news by its ID.
 */
class GetNewsByIdUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /**
     * Invoke the use case to get a specific news by its ID.
     *
     * @param id The ID of the news to retrieve.
     * @return A Flow emitting a Result containing the requested News.
     */
    operator fun invoke(id: Long): Flow<Result<News>> {
        return newsRepository.getNewsById(id)
    }
}