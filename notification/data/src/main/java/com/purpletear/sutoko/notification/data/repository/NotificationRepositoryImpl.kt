package com.purpletear.sutoko.notification.data.repository

import com.purpletear.sutoko.notification.model.Notification
import com.purpletear.sutoko.notification.repository.NotificationRepository
import com.purpletear.sutoko.notification.sealed.Screen
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationRepositoryImpl : NotificationRepository {

    private var _notification: MutableStateFlow<Notification?> = MutableStateFlow(null)
    override val notification: MutableStateFlow<Notification?> = _notification

    private var _screen: MutableStateFlow<Screen> = MutableStateFlow(Screen.Unspecified)

    override fun sendNotification(notification: Notification) {
        if (_screen.value != Screen.Unspecified && _screen.value == notification.screen && _screen.value.id == notification.screen.id) {
            // The user is already on this screen
            return
        }
        _notification.value = notification
    }

    override fun setCurrentScreen(screen: Screen) {
        _screen.value = screen
    }
}