package fr.purpletear.sutoko.sync.balance

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException

class BalanceSyncCoordinatorTest {

    private class FakeShopRepository(failuresBeforeSuccess: Int = 0) : ShopRepository {
        val balance = MutableStateFlow(Balance(coins = -1, diamonds = -1))
        val loadBalanceCalls = mutableListOf<String>()
        private var remainingFailures = failuresBeforeSuccess

        override fun observeBalance(): Flow<Balance> = balance

        override fun loadBalance(userId: String, userToken: String): Flow<Result<Unit>> = flow {
            loadBalanceCalls += userId
            if (remainingFailures > 0) {
                remainingFailures--
                emit(Result.failure(IOException("boom")))
            } else {
                balance.value = Balance(coins = 1500, diamonds = 1500)
                emit(Result.success(Unit))
            }
        }

        override fun resetBalance() {
            balance.value = Balance(coins = -1, diamonds = -1)
        }

        override fun updateBalance(balance: Balance) {
            this.balance.value = balance
        }

        override suspend fun getPacks(): Result<List<ShopPack>> = Result.success(emptyList())
    }

    private class FakeUserRepository : UserRepository {
        val userFlow = MutableStateFlow<User?>(null)
        override fun observeUser(): Flow<User?> = userFlow
        override fun observeIsConnected(): Flow<Boolean> = MutableStateFlow(userFlow.value != null)
        override fun isConnected(): Result<Boolean> = Result.success(userFlow.value != null)
        override suspend fun connect(id: String, token: String): Result<Unit> = Result.success(Unit)
        override suspend fun disconnect(): Result<Unit> = Result.success(Unit)
    }

    private class FakeLifecycle : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) = Unit
        override fun removeObserver(observer: LifecycleObserver) = Unit
        override val currentState: State get() = State.STARTED
    }

    @Test
    fun `failed loads are retried with backoff until success`() = runTest {
        val shopRepository = FakeShopRepository(failuresBeforeSuccess = 2)
        val userRepository = FakeUserRepository()
        val coordinator = BalanceSyncCoordinator(shopRepository, userRepository)
        coordinator.start(FakeLifecycle(), backgroundScope)

        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        runCurrent()
        assertEquals(1, shopRepository.loadBalanceCalls.size)
        testScheduler.advanceTimeBy(1_000L)
        runCurrent()
        assertEquals(2, shopRepository.loadBalanceCalls.size)
        testScheduler.advanceTimeBy(2_000L)
        runCurrent()

        assertEquals(3, shopRepository.loadBalanceCalls.size)
        assertEquals(1500, shopRepository.balance.first().coins)
    }

    @Test
    fun `user re-emission with the same id does not trigger a reload`() = runTest {
        val shopRepository = FakeShopRepository()
        val userRepository = FakeUserRepository()
        val coordinator = BalanceSyncCoordinator(shopRepository, userRepository)
        coordinator.start(FakeLifecycle(), backgroundScope)

        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        runCurrent()
        userRepository.userFlow.value = User(id = "user-1", token = "token-2")
        runCurrent()

        assertEquals(1, shopRepository.loadBalanceCalls.size)
    }

    @Test
    fun `exhausting retries stops without crashing`() = runTest {
        val shopRepository = FakeShopRepository(failuresBeforeSuccess = Int.MAX_VALUE)
        val userRepository = FakeUserRepository()
        val coordinator = BalanceSyncCoordinator(shopRepository, userRepository)
        coordinator.start(FakeLifecycle(), backgroundScope)

        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        runCurrent()
        testScheduler.advanceTimeBy(1_000L)
        runCurrent()
        testScheduler.advanceTimeBy(2_000L)
        runCurrent()
        testScheduler.advanceTimeBy(4_000L)
        runCurrent()

        assertEquals(4, shopRepository.loadBalanceCalls.size)
        assertFalse(shopRepository.balance.first().isLoaded())
    }
}
