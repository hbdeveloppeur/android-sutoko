package com.purpletear.sutoko.domain.usecase

import com.purpletear.sutoko.domain.repository.UserRepository
import javax.inject.Inject

class IsUserConnectedUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Result<Boolean> {
        return userRepository.isConnected()
    }
}
