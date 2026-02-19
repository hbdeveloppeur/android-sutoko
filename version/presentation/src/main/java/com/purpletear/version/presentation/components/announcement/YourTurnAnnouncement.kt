package com.purpletear.version.presentation.components.announcement

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.purpletear.core.presentation.util.openAppInStore
import com.purpletear.version.presentation.R

/**
 * Pre-configured announcement card for "Your Turn" version.
 *
 * @param onDismiss Callback when user dismisses the card
 * @param modifier Modifier for customizing the layout
 * @param viewModel ViewModel instance (injected by Hilt by default)
 */
@Composable
fun YourTurnAnnouncementCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VersionAnnouncementViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.load(YOUR_TURN_CONFIG)
    }

    val callbacks = object : VersionAnnouncementCallbacks {
        override fun onDismiss() {
            onDismiss()
        }

        override fun onRemindMeClick() {
            viewModel.toggleReminder(
                remindMeButtonText = YOUR_TURN_CONFIG.remindMeButtonText,
                reminderSetText = YOUR_TURN_CONFIG.reminderSetText,
                reminderSetIcon = YOUR_TURN_CONFIG.reminderSetIcon
            )
        }

        override fun onUpdateClick() {
            context.openAppInStore()
            onDismiss()
        }
    }

    VersionAnnouncementCard(
        state = viewModel.state,
        callbacks = callbacks,
        modifier = modifier
    )
}

/**
 * Configuration for "Your Turn" announcement.
 */
private val YOUR_TURN_CONFIG = VersionAnnouncementConfig(
    versionName = "Your Turn",
    title = "Create your own stories",
    subtitle = "AI-powered story creation coming soon",
    videoUrl = "https://data.sutoko.app/resources/sutoko-ai/video/anounce_your_turn.optimized.mp4",
    remindMeButtonText = "Remind me",
    reminderSetText = "Reminder activated",
    updateButtonText = "Update now",
    remindMeIcon = R.drawable.version_ic_notification,
    reminderSetIcon = R.drawable.version_rounded_check_circle_24
)
