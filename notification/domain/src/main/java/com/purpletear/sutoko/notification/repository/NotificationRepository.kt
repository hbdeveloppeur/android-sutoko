package com.purpletear.sutoko.notification.repository

import com.purpletear.sutoko.notification.model.Notification
import com.purpletear.sutoko.notification.sealed.Screen
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notification: StateFlow<Notification?>
    fun sendNotification(notification: Notification)
    fun setCurrentScreen(screen: Screen)
}