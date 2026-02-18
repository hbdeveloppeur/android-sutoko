package com.purpletear.sutoko.notification.usecase

import com.purpletear.sutoko.notification.model.Notification
import com.purpletear.sutoko.notification.repository.NotificationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveNotificationRequestUseCase @Inject constructor(
    val repository: NotificationRepository,
) {
    operator fun invoke(): StateFlow<Notification?> {
        return repository.notification
    }
}