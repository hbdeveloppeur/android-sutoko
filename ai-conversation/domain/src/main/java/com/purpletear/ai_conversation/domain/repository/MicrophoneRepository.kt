package com.purpletear.ai_conversation.domain.repository

import java.io.File

interface MicrophoneRepository {
    suspend fun startRecording(): Result<Unit>
    suspend fun stopRecording(): Result<Unit>
    fun isRecording(): Boolean
    suspend fun getRecordingFile(): File
    suspend fun clearRecordingDir(): Result<Unit>
}


