package fr.purpletear.sutoko.sync.catalog

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
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

    private val syncMutex = Mutex()
    private val _status = MutableStateFlow(CatalogSyncStatus.Loading)
    val status = _status.asStateFlow()

    fun start(lifecycle: Lifecycle, scope: CoroutineScope) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    scope.launch { sync() }
                }
            }
        )
    }

    suspend fun sync() {
        if (!syncMutex.tryLock()) return
        try {
            _status.value = CatalogSyncStatus.Loading
            val languageTag = Locale.getDefault().toLanguageTag()
            val result = gameRepository.syncOfficialGames(languageTag)
            _status.value = if (result.isSuccess) CatalogSyncStatus.Ready else CatalogSyncStatus.Failed
            result.onSuccess {
                val catalogs = gameRepository.observeOfficialGames().first()
                gameInstallRepository.ensureBuiltInGamesInstalled(catalogs)
            }.onFailure {
                Log.w("CatalogSyncCoordinator", "Catalog sync failed", it)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.w("CatalogSyncCoordinator", "Catalog sync failed", error)
        } finally {
            if (_status.value == CatalogSyncStatus.Loading) _status.value = CatalogSyncStatus.Failed
            syncMutex.unlock()
        }
    }
}

enum class CatalogSyncStatus { Loading, Ready, Failed }
