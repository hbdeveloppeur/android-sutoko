package com.purpletear.aiconversation.domain.messaging

interface MessageHandler {
    suspend fun handleMessage(data: Map<String, String>)
}