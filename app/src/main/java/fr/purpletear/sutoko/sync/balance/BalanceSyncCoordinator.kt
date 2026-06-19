package fr.purpletear.sutoko.sync.balance

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates balance synchronization at the application lifecycle boundary.
 *
 * Triggers sync when the app returns to foreground.
 */
@Singleton
class BalanceSyncCoordinator @Inject constructor(
    private val shopRepository: ShopRepository,
    private val userRepository: UserRepository,
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
        val user = userRepository.observeUser().firstOrNull() ?: return

        shopRepository.loadBalance(
            userId = user.id,
            userToken = user.token,
        )
            .collect { result ->
                result.onFailure {
                    Log.w("BalanceSyncCoordinator", "Balance sync failed", it)
                }
            }
    }
}
