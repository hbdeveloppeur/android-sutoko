package fr.purpletear.sutoko.sync.usergames

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates user-created games synchronization at the application lifecycle boundary.
 *
 * Triggers sync when the app returns to foreground.
 */
@Singleton
class UserGamesSyncCoordinator @Inject constructor(
    private val gameRepository: GameRepository,
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
        gameRepository.syncUserGames(languageTag)
            .onFailure { Log.w("UserGamesSyncCoordinator", "User games sync failed", it) }
    }
}
