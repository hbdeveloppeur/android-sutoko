package com.purpletear.sutoko.shop.presentation

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import com.purpletear.sutoko.shop.domain.usecase.GetShopPackPricesUseCase
import com.purpletear.sutoko.shop.domain.usecase.GetShopPacksUseCase
import com.purpletear.sutoko.shop.domain.usecase.ObserveShopBalanceUseCase
import com.purpletear.sutoko.shop.test.FakePurchaseRepository
import fr.sutoko.inapppurchase.application.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeUserRepository = FakeUserRepository()
    private val fakeShopRepository = FakeShopRepository()
    private val fakePurchaseRepository = FakePurchaseRepository()
    private val observeShopBalanceUseCase = ObserveShopBalanceUseCase(fakeShopRepository)
    private val getShopPacksUseCase = GetShopPacksUseCase(fakeShopRepository)
    private val getShopPackPricesUseCase = GetShopPackPricesUseCase(
        getShopPacksUseCase = getShopPacksUseCase,
        purchaseRepository = fakePurchaseRepository,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads packs with prices on init`() = runTest(testDispatcher) {
        fakeShopRepository.packs = listOf(
            ShopPack(coins = 100, diamonds = 100, sku = "low", type = CoinsPackType.Low),
            ShopPack(coins = 500, diamonds = 500, sku = "high", type = CoinsPackType.High),
        )
        fakePurchaseRepository.queryProductDetailsResult = mapOf(
            "low" to Result.success(product("low", "$0.99")),
            "high" to Result.success(product("high", "$4.99")),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.packs.value.size)
        assertEquals("$0.99", priceFor(viewModel, CoinsPackType.Low))
        assertEquals("$4.99", priceFor(viewModel, CoinsPackType.High))
    }

    @Test
    fun `reloads packs when billing connection becomes available`() = runTest(testDispatcher) {
        fakeShopRepository.packs = listOf(
            ShopPack(coins = 100, diamonds = 100, sku = "low", type = CoinsPackType.Low),
        )
        fakePurchaseRepository.queryProductDetailsResult = emptyMap()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.packs.value.size)
        assertEquals(null, priceFor(viewModel, CoinsPackType.Low))
        assertEquals(1, fakePurchaseRepository.queryProductDetailsCallCount)

        fakePurchaseRepository.queryProductDetailsResult = mapOf(
            "low" to Result.success(product("low", "$1.99"))
        )
        fakePurchaseRepository.connectionStateFlow.value = true
        advanceUntilIdle()

        assertEquals("$1.99", priceFor(viewModel, CoinsPackType.Low))
        assertEquals(2, fakePurchaseRepository.queryProductDetailsCallCount)
    }

    @Test
    fun `buy fails when pack has no price`() = runTest(testDispatcher) {
        fakeShopRepository.packs = listOf(
            ShopPack(coins = 100, diamonds = 100, sku = "low", type = CoinsPackType.Low),
        )
        fakePurchaseRepository.queryProductDetailsResult = emptyMap()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<ShopPurchaseEvent>()
        backgroundScope.launch {
            viewModel.purchaseEvents.collect { events.add(it) }
        }
        advanceUntilIdle()

        viewModel.onEvent(ShopEvent.BuyPack(CoinsPackType.Low))
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is ShopPurchaseEvent.Failed)
    }

    @Test
    fun `isUserConnected initial value reflects repository`() = runTest(testDispatcher) {
        fakeUserRepository.isConnectedFlow.value = true

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.isUserConnected.value)
    }

    @Test
    fun `header state is Disconnected when user is not connected`() = runTest(testDispatcher) {
        fakeUserRepository.isConnectedFlow.value = false
        fakeShopRepository.balanceFlow.value = Balance(coins = 100, diamonds = 50)

        val viewModel = createViewModel()
        val states = mutableListOf<ShopHeaderState>()
        backgroundScope.launch { viewModel.headerState.collect { states.add(it) } }
        advanceUntilIdle()

        assertEquals(ShopHeaderState.Disconnected, states.last())
    }

    @Test
    fun `header state is Loading when connected but balance not loaded`() = runTest(testDispatcher) {
        fakeUserRepository.isConnectedFlow.value = true
        fakeShopRepository.balanceFlow.value = Balance(coins = -1, diamonds = -1)

        val viewModel = createViewModel()
        val states = mutableListOf<ShopHeaderState>()
        backgroundScope.launch { viewModel.headerState.collect { states.add(it) } }
        advanceUntilIdle()

        assertEquals(ShopHeaderState.Loading, states.last())
    }

    @Test
    fun `header state is Loaded when connected and balance loaded`() = runTest(testDispatcher) {
        val balance = Balance(coins = 100, diamonds = 50)
        fakeUserRepository.isConnectedFlow.value = true
        fakeShopRepository.balanceFlow.value = balance

        val viewModel = createViewModel()
        val states = mutableListOf<ShopHeaderState>()
        backgroundScope.launch { viewModel.headerState.collect { states.add(it) } }
        advanceUntilIdle()

        assertEquals(ShopHeaderState.Loaded(balance), states.last())
    }

    @Test
    fun `header state resets to Disconnected on logout`() = runTest(testDispatcher) {
        val balance = Balance(coins = 100, diamonds = 50)
        fakeUserRepository.isConnectedFlow.value = true
        fakeShopRepository.balanceFlow.value = balance

        val viewModel = createViewModel()
        val states = mutableListOf<ShopHeaderState>()
        backgroundScope.launch { viewModel.headerState.collect { states.add(it) } }
        advanceUntilIdle()

        assertEquals(ShopHeaderState.Loaded(balance), states.last())

        fakeUserRepository.isConnectedFlow.value = false
        fakeShopRepository.balanceFlow.value = Balance(coins = -1, diamonds = -1)
        advanceUntilIdle()

        assertEquals(ShopHeaderState.Disconnected, states.last())
    }

    private fun createViewModel() = ShopViewModel(
        userRepository = fakeUserRepository,
        observeShopBalanceUseCase = observeShopBalanceUseCase,
        getShopPackPricesUseCase = getShopPackPricesUseCase,
        purchaseRepository = fakePurchaseRepository,
    )

    private fun priceFor(viewModel: ShopViewModel, type: CoinsPackType): String? {
        return viewModel.packs.value.firstOrNull { it.pack.type == type }?.formattedPrice
    }

    private fun product(sku: String, price: String) = Product(
        sku = sku,
        title = sku,
        description = sku,
        formattedPrice = price,
    )

    private class FakeUserRepository : UserRepository {
        val isConnectedFlow = MutableStateFlow(false)

        override fun observeUser(): Flow<User?> = flowOf(null)
        override fun observeIsConnected(): Flow<Boolean> = isConnectedFlow
        override fun isConnected(): Result<Boolean> = Result.success(isConnectedFlow.value)
        override suspend fun connect(id: String, token: String): Result<Unit> = Result.success(Unit)
        override suspend fun disconnect(): Result<Unit> = Result.success(Unit)
    }

    private class FakeShopRepository : ShopRepository {
        var packs: List<ShopPack> = emptyList()
        val balanceFlow = MutableStateFlow(Balance(coins = 0, diamonds = 0))

        override fun observeBalance(): Flow<Balance> = balanceFlow
        override fun loadBalance(userId: String, userToken: String): Flow<Result<Unit>> =
            flowOf(Result.success(Unit))

        override fun resetBalance() {
            balanceFlow.value = Balance(coins = -1, diamonds = -1)
        }

        override fun updateBalance(balance: Balance) {
            balanceFlow.value = balance
        }

        override suspend fun getPacks(): Result<List<ShopPack>> = Result.success(packs)
    }
}
