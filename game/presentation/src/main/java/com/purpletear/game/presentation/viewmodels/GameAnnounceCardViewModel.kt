package com.purpletear.game.presentation.viewmodels

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
 * Hilt-enabled ViewModel for the Game Announce card.
 * Loads the release publish date via GetVersionByNameUseCase.
 */
@HiltViewModel
class GameAnnounceCardViewModel @Inject constructor(
    private val isReminderNotificationEnabledUseCase: IsReminderNotificationEnabledUseCase,
    private val setReminderNotificationEnabledUseCase: EnableReminderNotificationUseCase,
    private val disableReminderNotificationUseCase: DisableReminderNotificationUseCase,
    private val getVersionByNameUseCase: GetVersionByNameUseCase,
) : ViewModel() {

    // Indicates whether the card is currently loading data
    var isLoading by mutableStateOf(true)
        private set

    // Release date of the game announcement in epoch millis
    var releaseDate by mutableStateOf<Long>(System.currentTimeMillis())
        private set

    // Whether the reminder notification is currently enabled
    var reminderEnabled by mutableStateOf(false)
        private set

    init {
        // Load initial reminder state
        reminderEnabled = isReminderNotificationEnabledUseCase()
    }

    /**
     * Load the release (Version) by name and update releaseDate from its publishDate.
     * - languageCode defaults to the current device language.
     */
    fun loadReleaseByName(name: String, languageCode: String = Locale.getDefault().language) {

        viewModelScope.launch {
            isLoading = true
            delay(680)
            try {
                val result = getVersionByNameUseCase(name, languageCode)
                result.onSuccess { version ->
                    releaseDate = version.publishDate
                }.onFailure {

                }
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleReminder() {
        if (reminderEnabled) {
            disableReminderNotificationUseCase()
            reminderEnabled = false
        } else {
            setReminderNotificationEnabledUseCase()
            reminderEnabled = true
        }
    }
}
