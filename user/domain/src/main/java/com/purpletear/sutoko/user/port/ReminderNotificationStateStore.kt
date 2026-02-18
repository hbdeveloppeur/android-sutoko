package com.purpletear.sutoko.user.port

interface ReminderNotificationStateStore {
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
}
