package fr.purpletear.sutoko.sync.catalog

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates game catalog synchronization at the application lifecycle boundary.
 *
 * Triggers sync when the app returns to foreground.
 */
@Singleton
class CatalogSyncCoordinator @Inject constructor(
    private val gameRepository: GameRepository,
    private val gameInstallRepository: GameInstallRepository,
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
        gameRepository.syncOfficialGames(languageTag)
            .onSuccess {
                val catalogs = gameRepository.observeOfficialGames().first()
                gameInstallRepository.ensureBuiltInGamesInstalled(catalogs)
            }
            .onFailure { Log.w("CatalogSyncCoordinator", "Catalog sync failed", it) }
    }
}
