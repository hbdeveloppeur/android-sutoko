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
 * Orchestrates balance synchronization at the application boundary.
 *
 * Triggers sync when the app returns to foreground and whenever the
 * authenticated user changes: loads the balance for a logged-in user and
 * resets it to the unloaded sentinel on logout, so the cached balance always
 * reflects the current user (or is explicitly unloaded).
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

        // React to auth changes within the running session: a foreground
        // onStart does not re-fire for an in-session login, so we observe the
        // user directly. Load on login, reset on logout.
        scope.launch {
            userRepository.observeUser().collect { user ->
                if (user == null) {
                    shopRepository.resetBalance()
                } else {
                    refresh(user.id, user.token)
                }
            }
        }
    }

    private suspend fun sync() {
        val user = userRepository.observeUser().firstOrNull() ?: return
        refresh(user.id, user.token)
    }

    private suspend fun refresh(userId: String, userToken: String) {
        shopRepository.loadBalance(
            userId = userId,
            userToken = userToken,
        )
            .collect { result ->
                result.onFailure {
                    Log.w("BalanceSyncCoordinator", "Balance sync failed", it)
                }
            }
    }
}
