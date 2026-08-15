package fr.purpletear.sutoko.sync.balance

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
 *
 * Failed loads are retried in-session with backoff (the user should not have
 * to restart the app to see their balance). Triggers are serialized: a newer
 * trigger supersedes in-flight retries.
 */
@Singleton
class BalanceSyncCoordinator @Inject constructor(
    private val shopRepository: ShopRepository,
    private val userRepository: UserRepository,
) {

    private var refreshJob: Job? = null

    fun start(lifecycle: Lifecycle, scope: CoroutineScope) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    scope.launch { sync(scope) }
                }
            }
        )

        // React to auth changes within the running session: a foreground
        // onStart does not re-fire for an in-session login, so we observe the
        // user directly. Load on login, reset on logout.
        scope.launch {
            userRepository.observeUser()
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collect { user ->
                    if (user == null) {
                        refreshJob?.cancel()
                        shopRepository.resetBalance()
                    } else {
                        scheduleRefresh(scope, user.id, user.token)
                    }
                }
        }
    }

    private suspend fun sync(scope: CoroutineScope) {
        val user = userRepository.observeUser().firstOrNull() ?: return
        scheduleRefresh(scope, user.id, user.token)
    }

    private fun scheduleRefresh(scope: CoroutineScope, userId: String, userToken: String) {
        refreshJob?.cancel()
        refreshJob = scope.launch { refreshWithRetry(userId, userToken) }
    }

    private suspend fun refreshWithRetry(userId: String, userToken: String) {
        repeat(MAX_ATTEMPTS) { attempt ->
            val result = shopRepository.loadBalance(
                userId = userId,
                userToken = userToken,
            ).first()
            if (result.isSuccess) return
            Log.w("BalanceSyncCoordinator", "Balance sync failed", result.exceptionOrNull())
            if (attempt < MAX_ATTEMPTS - 1) {
                delay(RETRY_DELAYS_MS[attempt])
            }
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 4
        val RETRY_DELAYS_MS = longArrayOf(1_000L, 2_000L, 4_000L)
    }
}
