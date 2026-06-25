package fr.purpletear.sutoko.screens.main.presentation

import com.purpletear.sutoko.news.model.News

/**
 * UI state for the news pager on the home screen.
 */
data class NewsState(
    val news: List<News> = emptyList(),
    val currentIndex: Int = 0
)
