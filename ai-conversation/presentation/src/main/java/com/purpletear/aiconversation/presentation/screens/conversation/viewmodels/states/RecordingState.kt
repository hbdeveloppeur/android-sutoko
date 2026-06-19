package com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.states

import androidx.annotation.Keep


sealed class RecordingState {
    @Keep
    data class StopRecording(val isCanceled: Boolean) : RecordingState()
    data object StartRecording : RecordingState()
}
