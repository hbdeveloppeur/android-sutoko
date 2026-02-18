package com.purpletear.sutoko.user.usecase

import com.purpletear.sutoko.user.repository.UserRepository
import javax.inject.Inject


class OpenSignInPageUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke() {
        userRepository.openSignInPage()
    }
}