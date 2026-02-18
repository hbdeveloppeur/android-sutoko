package com.purpletear.sutoko.notification.usecase

import com.purpletear.sutoko.notification.model.Notification
import com.purpletear.sutoko.notification.repository.NotificationRepository
import com.purpletear.sutoko.notification.sealed.Screen
import javax.inject.Inject

class SendNotificationUseCase @Inject constructor(
    val repository: NotificationRepository,
) {
    operator fun invoke(
        title: String,
        message: String,
        imageUrl: String?,
        destination: String,
        screen: Screen,
    ) {
        val notification = Notification(title, message, imageUrl, destination, screen)
        repository.sendNotification(notification)
    }
}