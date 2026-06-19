package com.purpletear.sutoko.auth.coordinator

import kotlinx.coroutines.flow.Flow

interface AuthCoordinator {
    fun requestSignIn()
    val events: Flow<AuthEvent>
}

sealed interface AuthEvent {
    object OpenSignIn : AuthEvent
}
