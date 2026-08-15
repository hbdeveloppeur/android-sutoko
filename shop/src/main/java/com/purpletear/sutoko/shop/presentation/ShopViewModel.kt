package com.purpletear.sutoko.shop.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import com.purpletear.sutoko.domain.exception.NotConnectedException
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.model.PackItem
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.usecase.GetShopPackPricesUseCase
import com.purpletear.sutoko.shop.domain.usecase.ObserveShopBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.model.PurchaseErrorType
import fr.sutoko.inapppurchase.application.domain.model.toPurchaseErrorType
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import fr.sutoko.inapppurchase.application.domain.usecase.PurchaseWithAuthCheckUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
    observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val getShopPackPricesUseCase: GetShopPackPricesUseCase,
    private val purchaseRepository: PurchaseRepository,
    private val purchaseWithAuthCheckUseCase: PurchaseWithAuthCheckUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    val balance: StateFlow<Balance> = observeShopBalanceUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Balance(coins = -1, diamonds = -1),
        )

    val isUserConnected: StateFlow<Boolean> = userRepository
        .observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = userRepository.isConnected().getOrDefault(false),
        )

    val headerState: StateFlow<ShopHeaderState> = combine(
        isUserConnected,
        balance,
    ) { connected, balance ->
        when {
            !connected -> ShopHeaderState.Disconnected
            balance.loadFailed && !balance.isLoaded() -> ShopHeaderState.Failed
            !balance.isLoaded() -> ShopHeaderState.Loading
            else -> ShopHeaderState.Loaded(balance)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShopHeaderState.Loading,
    )

    private val _packs = MutableStateFlow<List<PackItem>>(emptyList())
    val packs: StateFlow<List<PackItem>> = _packs.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<ShopPurchaseEvent>(extraBufferCapacity = 1)
    val purchaseEvents: SharedFlow<ShopPurchaseEvent> = _purchaseEvents.asSharedFlow()

    init {
        analyticsTracker.logEvent("shop_view")

        viewModelScope.launch {
            loadPacks()
        }

        viewModelScope.launch {
            purchaseRepository.connectionState
                .distinctUntilChanged()
                .filter { it }
                .collect { loadPacks() }
        }

        viewModelScope.launch {
            purchaseRepository.purchaseProcessing.collect { sku ->
                val packType = _packs.value
                    .firstOrNull { it.pack.sku == sku }
                    ?.pack
                    ?.type
                if (packType != null) {
                    _purchaseEvents.emit(ShopPurchaseEvent.Processing(packType))
                }
            }
        }
    }

    private suspend fun loadPacks() {
        getShopPackPricesUseCase()
            .onSuccess { _packs.value = it }
    }

    /**
     * Retries the balance load after a failure. Only acts when the last load
     * actually failed, so normal loading and loaded states are never disturbed.
     */
    fun retryBalanceLoad() {
        if (!balance.value.loadFailed) return
        refreshBalance()
    }

    /**
     * Refreshes the balance from the backend. Called after a successful
     * purchase (covering the window until backend registration pushes the
     * credited balance) and on manual retry after a failure.
     */
    private fun refreshBalance() {
        viewModelScope.launch {
            val user = userRepository.observeUser().firstOrNull() ?: return@launch
            shopRepository.loadBalance(user.id, user.token).collect { result ->
                result.onFailure { Log.w("ShopViewModel", "Balance refresh failed", it) }
            }
        }
    }

    fun onEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.BuyPack -> {
                val packType = event.packType
                viewModelScope.launch { buy(packType) }
            }
        }
    }

    private suspend fun buy(packType: CoinsPackType) {
        val packItem = _packs.value.firstOrNull { it.pack.type == packType }
        if (packItem == null || packItem.formattedPrice.isNullOrBlank()) {
            _purchaseEvents.emit(ShopPurchaseEvent.Failed(packType, "Pack not available"))
            return
        }

        _purchaseEvents.emit(ShopPurchaseEvent.Started(packType))

        purchaseWithAuthCheckUseCase(sku = packItem.pack.sku)
            .onSuccess {
                _purchaseEvents.emit(ShopPurchaseEvent.Success(packType))
                refreshBalance()
            }
            .onFailure { error ->
                val event = when {
                    error is NotConnectedException -> ShopPurchaseEvent.NotConnected(packType)
                    else -> when (error.toPurchaseErrorType()) {
                        PurchaseErrorType.PENDING -> ShopPurchaseEvent.Pending(packType)
                        PurchaseErrorType.CANCELLED -> ShopPurchaseEvent.Cancelled(packType)
                        PurchaseErrorType.ALREADY_OWNED -> ShopPurchaseEvent.AlreadyOwned(packType)
                        PurchaseErrorType.FAILED,
                        PurchaseErrorType.UNKNOWN -> ShopPurchaseEvent.Failed(packType, error.message)
                    }
                }
                _purchaseEvents.emit(event)
            }
    }
}
