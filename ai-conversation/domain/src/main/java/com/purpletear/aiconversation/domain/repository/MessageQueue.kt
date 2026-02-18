package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import kotlinx.coroutines.flow.StateFlow

interface MessageQueue {
    val messages: StateFlow<List<Message>>
    fun acknowledge(ids: List<String>)
    fun cancelTimer()
    fun startTimer(onTick: (messages: List<Message>) -> Unit)
    fun add(message: Message)
    fun clear()
    fun remove(predicate: (Message) -> Boolean)
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun mark(state: MessageState)
}