package com.purpletear.sutoko.news.usecase

import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing cached news.
 */
class ObserveNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /**
     * Invoke the use case to observe news.
     *
     * @return A Flow emitting the list of News.
     */
    operator fun invoke(): Flow<List<News>> {
        return newsRepository.observeNews()
    }
}
