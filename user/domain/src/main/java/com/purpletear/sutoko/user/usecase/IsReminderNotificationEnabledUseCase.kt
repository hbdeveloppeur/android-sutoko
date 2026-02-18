package com.purpletear.sutoko.user.usecase

import com.purpletear.sutoko.user.port.ReminderNotificationStateStore
import javax.inject.Inject

class IsReminderNotificationEnabledUseCase @Inject constructor(
    private val stateStore: ReminderNotificationStateStore,
) {
    operator fun invoke(): Boolean = stateStore.isEnabled()
}
