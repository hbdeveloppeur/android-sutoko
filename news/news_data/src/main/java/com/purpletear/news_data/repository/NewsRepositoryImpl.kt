package com.purpletear.news_data.repository

import com.purpletear.news_data.remote.NewsApiWrapper
import com.purpletear.news_data.remote.dto.toDomain
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of the NewsRepository interface.
 */
class NewsRepositoryImpl @Inject constructor(
    private val apiWrapper: NewsApiWrapper,
    private val appVersionProvider: AppVersionProvider
) : NewsRepository {

    // Thread-safe and observable cache
    private val newsStateFlow = MutableStateFlow<List<News>?>(null)

    /**
     * Get a list of all news.
     *
     * @return A Flow emitting a Result containing a list of News.
     */
    override fun getNews(): Flow<Result<List<News>>> = flow {
        try {
            // Return cached value if available
            newsStateFlow.value?.let {
                emit(Result.success(it))
            } ?: run {
                // Fetch from API (first load)
                val langCode = java.util.Locale.getDefault().language
                val response = apiWrapper.getNews(langCode, appVersionProvider.getVersionCode())
                if (response.isSuccessful) {
                    val news = response.body()?.toDomain() ?: emptyList()
                    newsStateFlow.value = news
                    android.util.Log.d("CRFAH", "Found ${news.size} news")
                    emit(Result.success(news))
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    android.util.Log.d("CRFAH", "Answer is not successful ${errorBody}")
                    val exception =
                        Exception("API call failed with code ${response.code()}: $errorBody")
                    emit(Result.failure(exception))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.d("CRFAH", "Answer is exception ${e.message}")
            emit(Result.failure(e))
        }
    }

    /**
     * Get a specific news by its ID.
     *
     * @param id The ID of the news to retrieve.
     * @return A Flow emitting a Result containing the requested News.
     */
    override fun getNewsById(id: Long): Flow<Result<News>> = flow {
        try {
            val response = apiWrapper.getNewsById(id)
            if (response.isSuccessful) {
                val news = response.body()?.toDomain()
                if (news != null) {
                    emit(Result.success(news))
                } else {
                    emit(Result.failure(Exception("News not found")))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                val exception =
                    Exception("API call failed with code ${response.code()}: $errorBody")
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Refresh the news data from the remote source.
     */
    override suspend fun refreshNews() {

        val langCode = java.util.Locale.getDefault().language
        val response = apiWrapper.getNews(langCode, appVersionProvider.getVersionCode())
        if (response.isSuccessful) {
            val apiNews = response.body()?.toDomain() ?: emptyList()
            newsStateFlow.value = apiNews
        }
    }

    /**
     * Observe the cached news data.
     *
     * @return A StateFlow emitting the cached list of News.
     */
    override fun observeCachedNews(): StateFlow<List<News>?> = newsStateFlow
}
