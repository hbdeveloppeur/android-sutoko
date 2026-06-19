package com.purpletear.sutoko.domain.usecase

import com.purpletear.sutoko.auth.coordinator.AuthCoordinator
import javax.inject.Inject


class OpenSignInPageUseCase @Inject constructor(
    private val authCoordinator: AuthCoordinator,
) {
    operator fun invoke() {
        authCoordinator.requestSignIn()
    }
}