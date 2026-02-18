package com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.states


sealed class RecordingState {
    data class StopRecording(val isCanceled: Boolean) : RecordingState()
    data object StartRecording : RecordingState()
}
