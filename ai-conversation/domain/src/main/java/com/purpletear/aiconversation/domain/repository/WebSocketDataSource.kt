package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.sealed.WebSocketMessage
import kotlinx.coroutines.flow.Flow

interface WebSocketDataSource {
    val isConnected: Boolean

    fun sendMessage(
        characterId: Int,
        messages: List<Message>,
        userId: String,
        token: String,
        userName: String?,
    ): Flow<Result<Unit>>

    fun connect(uid: String, token: String): Flow<WebSocketMessage>
    fun send(message: String): Boolean
    fun sendPong(uid: String, token: String)
}