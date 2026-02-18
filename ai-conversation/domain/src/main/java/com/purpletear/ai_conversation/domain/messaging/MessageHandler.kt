package com.purpletear.ai_conversation.domain.messaging

interface MessageHandler {
    suspend fun handleMessage(data: Map<String, String>)
}