package com.purpletear.ai_conversation.domain.repository

import kotlinx.coroutines.flow.Flow
import java.io.File

interface MessageRepository {

    suspend fun sendMessage(
        uid: String,
        token: String,
        characterId: Int,
        texts: List<String>,
        audioFiles: List<File>,
        userName: String?
    ): Flow<Result<Unit>>
}