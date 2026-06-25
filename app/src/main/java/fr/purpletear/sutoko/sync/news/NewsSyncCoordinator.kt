package fr.purpletear.sutoko.sync.news

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.news.repository.NewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates news synchronization at the application lifecycle boundary.
 *
 * Triggers sync when the app returns to foreground.
 */
@Singleton
class NewsSyncCoordinator @Inject constructor(
    private val newsRepository: NewsRepository,
) {

    fun start(lifecycle: Lifecycle, scope: CoroutineScope) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    scope.launch { sync() }
                }
            }
        )
    }

    private suspend fun sync() {
        val languageTag = Locale.getDefault().toLanguageTag()
        newsRepository.syncNews(languageTag)
            .onFailure { Log.w("NewsSyncCoordinator", "News sync failed", it) }
    }
}
