package fr.purpletear.sutoko.screens.create

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import kotlinx.coroutines.launch
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.usecase.ObserveShopBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val customer: Customer,
) : ViewModel() {

    private val _balance = mutableStateOf<Resource<Balance>>(Resource.Loading())
    val balance: State<Resource<Balance>> = _balance

    init {
        observeBalance()
    }

    private fun observeBalance() {
        _balance.value = Resource.Loading()
        viewModelScope.launch {
            executeFlowUseCase(
                { observeShopBalanceUseCase() },
                onStream = { balance ->
                    balance?.let {
                        _balance.value = Resource.Success(it)
                    }
                },
                onFailure = { exception ->
                    _balance.value = Resource.Error(exception)
                }
            )
        }
    }

    fun getCoins(): Int = customer.getCoins()

    fun getDiamonds(): Int = customer.getDiamonds()

    fun isUserConnected(): Boolean = customer.isUserConnected()
}
