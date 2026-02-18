package com.purpletear.sutoko.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.purpletear.sutoko.user.repository.UserRepository
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserRepositoryImpl(initialConnectionState: Boolean, private val customer: Customer) :
    UserRepository {
    private var _isConnected = MutableStateFlow(initialConnectionState)
    override val isConnected: StateFlow<Boolean> get() = _isConnected
    private val _navigateToAccount = MutableLiveData<Unit>()
    override val navigateToAccount: LiveData<Unit> get() = _navigateToAccount

    override fun openSignInPage() {
        _navigateToAccount.value = Unit
    }

    override fun onLoginSuccess() {
        _isConnected.value = true
        customer.readNoContext()
    }

    override fun disconnect() {
        _isConnected.value = false
    }

}