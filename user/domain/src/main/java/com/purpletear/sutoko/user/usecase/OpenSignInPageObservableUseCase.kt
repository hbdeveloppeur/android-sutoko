package com.purpletear.sutoko.user.usecase

import androidx.lifecycle.LiveData
import com.purpletear.sutoko.user.repository.UserRepository
import javax.inject.Inject


class OpenSignInPageObservableUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke() : LiveData<Unit> {
        return userRepository.navigateToAccount
    }
}