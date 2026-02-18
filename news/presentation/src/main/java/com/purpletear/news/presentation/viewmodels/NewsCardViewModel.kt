package com.purpletear.news.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.sutoko.news.model.News
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the NewsCard component.
 * Injects HostProvider to access the host name for API requests.
 */
@HiltViewModel
class NewsCardViewModel @Inject constructor(
    private val hostProvider: HostProvider
) : ViewModel() {

    fun getImageLink(news: News): String {
        return hostProvider.getPublicMedia(filename = news.media.filename)
    }
}
