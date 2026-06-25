package com.purpletear.sutoko.shop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.model.PackItem
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.usecase.GetShopPackPricesUseCase
import com.purpletear.sutoko.shop.domain.usecase.ObserveShopBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.model.PurchaseErrorType
import fr.sutoko.inapppurchase.application.domain.model.toPurchaseErrorType
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val userRepository: UserRepository,
    observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val getShopPackPricesUseCase: GetShopPackPricesUseCase,
    private val purchaseRepository: PurchaseRepository,
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
            initialValue = false,
        )

    private val _packs = MutableStateFlow<List<PackItem>>(emptyList())
    val packs: StateFlow<List<PackItem>> = _packs.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<ShopPurchaseEvent>(extraBufferCapacity = 1)
    val purchaseEvents: SharedFlow<ShopPurchaseEvent> = _purchaseEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            loadPacks()
        }

        viewModelScope.launch {
            purchaseRepository.connectionState
                .distinctUntilChanged()
                .filter { it }
                .collect { loadPacks() }
        }
    }

    private suspend fun loadPacks() {
        getShopPackPricesUseCase()
            .onSuccess { _packs.value = it }
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

        purchaseRepository.purchase(sku = packItem.pack.sku)
            .onSuccess {
                _purchaseEvents.emit(ShopPurchaseEvent.Success(packType))
            }
            .onFailure { error ->
                val event = when (error.toPurchaseErrorType()) {
                    PurchaseErrorType.PENDING -> ShopPurchaseEvent.Pending(packType)
                    PurchaseErrorType.CANCELLED -> ShopPurchaseEvent.Cancelled(packType)
                    PurchaseErrorType.ALREADY_OWNED -> ShopPurchaseEvent.AlreadyOwned(packType)
                    PurchaseErrorType.FAILED,
                    PurchaseErrorType.UNKNOWN -> ShopPurchaseEvent.Failed(packType, error.message)
                }
                _purchaseEvents.emit(event)
            }
    }
}
