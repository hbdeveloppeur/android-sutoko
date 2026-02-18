package com.purpletear.sutoko.notification.usecase

import com.purpletear.sutoko.notification.repository.NotificationRepository
import com.purpletear.sutoko.notification.sealed.Screen
import javax.inject.Inject

class SetCurrentScreenUseCase @Inject constructor(
    val repository: NotificationRepository
) {
    operator fun invoke(
        screen: Screen,
    ) {
        repository.setCurrentScreen(screen)
    }
}