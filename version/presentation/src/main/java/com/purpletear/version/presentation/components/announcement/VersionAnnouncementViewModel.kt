package com.purpletear.version.presentation.components.announcement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.user.usecase.DisableReminderNotificationUseCase
import com.purpletear.sutoko.user.usecase.EnableReminderNotificationUseCase
import com.purpletear.sutoko.user.usecase.IsReminderNotificationEnabledUseCase
import com.purpletear.sutoko.version.usecase.GetVersionByNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Hilt-enabled ViewModel for managing version announcement card state.
 *
 * @param isReminderNotificationEnabledUseCase Use case to check reminder status
 * @param setReminderNotificationEnabledUseCase Use case to enable reminder
 * @param disableReminderNotificationUseCase Use case to disable reminder
 * @param getVersionByNameUseCase Use case to fetch version info
 */
@HiltViewModel
class VersionAnnouncementViewModel @Inject constructor(
    private val isReminderNotificationEnabledUseCase: IsReminderNotificationEnabledUseCase,
    private val setReminderNotificationEnabledUseCase: EnableReminderNotificationUseCase,
    private val disableReminderNotificationUseCase: DisableReminderNotificationUseCase,
    private val getVersionByNameUseCase: GetVersionByNameUseCase,
) : ViewModel() {

    var state by mutableStateOf(VersionAnnouncementState(
        title = "",
        subtitle = "",
        releaseDate = System.currentTimeMillis(),
        videoUrl = "",
        isAvailable = false,
        isLoading = true,
        isReminderEnabled = false,
        buttonText = "",
        buttonLeadingIcon = null
    ))
        private set

    init {
        state = state.copy(isReminderEnabled = isReminderNotificationEnabledUseCase())
    }

    /**
     * Load version announcement data by version name.
     *
     * @param config Configuration for the announcement card (title, subtitle, video URL)
     * @param languageCode Language code for localization (defaults to device locale)
     */
    fun load(
        config: VersionAnnouncementConfig,
        languageCode: String = Locale.getDefault().language
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            delay(680) // Artificial delay for smooth loading animation

            try {
                val result = getVersionByNameUseCase(config.versionName, languageCode)
                result.onSuccess { version ->
                    val releaseDate = version.publishDate
                    val isAvailable = isTodayOrEarlier(releaseDate)

                    state = state.copy(
                        title = config.title,
                        subtitle = config.subtitle,
                        releaseDate = releaseDate,
                        videoUrl = config.videoUrl,
                        isAvailable = isAvailable,
                        isLoading = false,
                        buttonText = if (isAvailable) {
                            config.updateButtonText
                        } else {
                            if (state.isReminderEnabled) config.reminderSetText else config.remindMeButtonText
                        },
                        buttonLeadingIcon = if (!isAvailable && state.isReminderEnabled) {
                            config.reminderSetIcon
                        } else if (!isAvailable) {
                            config.remindMeIcon
                        } else {
                            null
                        }
                    )
                }.onFailure {
                    state = state.copy(isLoading = false)
                }
            } finally {
                if (state.isLoading) {
                    state = state.copy(isLoading = false)
                }
            }
        }
    }

    /**
     * Toggle reminder notification state.
     */
    fun toggleReminder(
        remindMeButtonText: String,
        reminderSetText: String,
        reminderSetIcon: Int?,
    ) {
        if (state.isReminderEnabled) {
            disableReminderNotificationUseCase()
            state = state.copy(
                isReminderEnabled = false,
                buttonText = remindMeButtonText,
                buttonLeadingIcon = null
            )
        } else {
            setReminderNotificationEnabledUseCase()
            state = state.copy(
                isReminderEnabled = true,
                buttonText = reminderSetText,
                buttonLeadingIcon = reminderSetIcon
            )
        }
    }

    private fun isTodayOrEarlier(timestamp: Long): Boolean {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        return timestamp <= today
    }
}

/**
 * Configuration data class for version announcement cards.
 *
 * @property versionName The name of the version to fetch
 * @property title The title displayed on the card
 * @property subtitle The subtitle displayed on the card
 * @property videoUrl URL for the background video
 * @property remindMeButtonText Text for "Remind me" button
 * @property reminderSetText Text shown when reminder is set
 * @property updateButtonText Text for "Update" button
 * @property remindMeIcon Icon for "Remind me" button
 * @property reminderSetIcon Icon shown when reminder is enabled
 */
data class VersionAnnouncementConfig(
    val versionName: String,
    val title: String,
    val subtitle: String,
    val videoUrl: String,
    val remindMeButtonText: String = "Remind me",
    val reminderSetText: String = "Reminder set",
    val updateButtonText: String = "Update",
    val remindMeIcon: Int? = null,
    val reminderSetIcon: Int? = null,
)
