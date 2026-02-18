package com.purpletear.sutoko.data.user

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.purpletear.sutoko.user.port.ReminderNotificationStateStore

class SharedPrefsReminderNotificationStateStore(
    context: Context
) : ReminderNotificationStateStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    override fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    override fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
    }

    private companion object {
        const val KEY_ENABLED = "reminder_notifications_enabled"
    }
}
