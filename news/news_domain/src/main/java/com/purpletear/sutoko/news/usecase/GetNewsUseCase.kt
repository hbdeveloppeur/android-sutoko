package com.purpletear.sutoko.news.usecase

import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all news.
 */
class GetNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /**
     * Invoke the use case to get all news.
     *
     * @return A Flow emitting a Result containing a list of News.
     */
    operator fun invoke(): Flow<Result<List<News>>> {
        return newsRepository.getNews()
    }
}