package com.purpletear.sutoko.user.usecase

import com.purpletear.sutoko.user.port.ReminderNotificationStateStore
import com.purpletear.sutoko.user.port.UserValuesUpdater
import javax.inject.Inject

class DisableReminderNotificationUseCase @Inject constructor(
    private val userValuesUpdater: UserValuesUpdater,
    private val stateStore: ReminderNotificationStateStore,
) {
    operator fun invoke() {
        // Save local state
        stateStore.setEnabled(false)
        // Reflect to analytics
        userValuesUpdater.setValue("want_to_get_notified", "no")
    }
}
