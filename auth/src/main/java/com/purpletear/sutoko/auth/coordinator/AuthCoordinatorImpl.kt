package com.purpletear.sutoko.auth.coordinator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthCoordinatorImpl @Inject constructor() : AuthCoordinator {

    private val _events = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    override val events: Flow<AuthEvent> = _events.asSharedFlow()

    override fun requestSignIn() {
        _events.tryEmit(AuthEvent.OpenSignIn)
    }
}
