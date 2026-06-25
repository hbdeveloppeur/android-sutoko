package com.purpletear.news.data.repository

import com.purpletear.news.data.remote.NewsApiWrapper
import com.purpletear.news.data.remote.dto.toDomain
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Locale
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
     * Observe the cached list of news.
     *
     * @return A Flow emitting the list of News.
     */
    override fun observeNews(): Flow<List<News>> = newsStateFlow.map { it ?: emptyList() }

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
     * Synchronize news with the remote source.
     *
     * @param languageTag The BCP-47 language tag to request news for.
     * @return A Result indicating success or failure of the sync operation.
     */
    override suspend fun syncNews(languageTag: String): Result<Unit> {
        return try {
            val langCode = Locale.forLanguageTag(languageTag).language
            val response = apiWrapper.getNews(langCode, appVersionProvider.getVersionCode())
            if (response.isSuccessful) {
                val news = response.body()?.toDomain() ?: emptyList()
                newsStateFlow.value = news
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                val exception =
                    Exception("API call failed with code ${response.code()}: $errorBody")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
