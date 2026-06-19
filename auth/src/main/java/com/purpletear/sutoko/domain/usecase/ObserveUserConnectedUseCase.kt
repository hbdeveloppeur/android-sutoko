package com.purpletear.sutoko.domain.usecase

import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserConnectedUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<Boolean> {
        return userRepository.observeIsConnected()
    }
}
