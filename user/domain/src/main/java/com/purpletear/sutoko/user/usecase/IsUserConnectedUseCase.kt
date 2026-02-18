package com.purpletear.sutoko.user.usecase

import com.purpletear.sutoko.user.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


class IsUserConnectedUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(
    ): StateFlow<Boolean> {
        return userRepository.isConnected
    }
}
