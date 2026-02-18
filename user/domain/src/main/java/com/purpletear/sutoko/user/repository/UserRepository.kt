package com.purpletear.sutoko.user.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val isConnected : StateFlow<Boolean>
    val navigateToAccount: LiveData<Unit>

    fun openSignInPage()
    fun onLoginSuccess()
    fun disconnect()
}