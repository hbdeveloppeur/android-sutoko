package com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.states


sealed class RecordingState {
    data class StopRecording(val isCanceled: Boolean) : RecordingState()
    data object StartRecording : RecordingState()
}
