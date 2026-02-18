package com.purpletear.ai_conversation.data.repository

import android.util.Log
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.repository.MessageQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import purpletear.fr.purpleteartools.DelayHandler

class MessageQueueImpl() : MessageQueue {
    private var _mutableQueue: MutableStateFlow<List<Message>> = MutableStateFlow(listOf())

    override val messages: StateFlow<List<Message>>
        get() {
            return _mutableQueue
        }

    private val delayHandler: DelayHandler = DelayHandler()
    private val waiterName: String = "waiter"

    private val waiterDuration: Int
        get() {
            return 1500
        }

    override fun acknowledge(ids: List<String>) {
        messages.value.forEachIndexed { index, message ->
            if (message.id in ids) {
                message.acknowledge()
                _mutableQueue.value = _mutableQueue.value.toMutableList().apply {
                    this[index] = message
                }
            }
        }
    }

    override fun cancelTimer() {
        Log.d("MessageQueue", "canceled")
        delayHandler.stop(waiterName)
    }

    override fun startTimer(onTick: (messages: List<Message>) -> Unit) {
        Log.d("MessageQueue", "Waiter started")
        delayHandler.stop(waiterName)
        delayHandler.operation(waiterName, waiterDuration) {
            Log.d("MessageQueue", "Waiter executed")
            if (_mutableQueue.value.isNotEmpty()) {
                onTick(_mutableQueue.value.toList())
            }
        }
    }

    override fun add(message: Message) {
        _mutableQueue.update {
            val list = it.toMutableList()
            list.add(message)
            list
        }
    }

    override fun remove(predicate: (Message) -> Boolean) {
        _mutableQueue.update {
            val list = it.toMutableList()
            list.removeIf(predicate)
            list
        }
    }

    override fun clear() {
        Log.d("MessageQueue", "Queue cleared")
        _mutableQueue.update {
            listOf()
        }
    }

    override fun isEmpty(): Boolean {
        return _mutableQueue.value.isEmpty()
    }

    override fun isNotEmpty(): Boolean {
        return _mutableQueue.value.isNotEmpty()
    }

    override fun mark(state: MessageState) {
        _mutableQueue.update { messages ->
            messages.map { message ->
                val index = _mutableQueue.value.indexOfFirst { it.id == message.id }
                if (index != -1 && message.hiddenState !in setOf(MessageState.Sent)) {
                    message.copy(hiddenState = state)
                    message
                } else {
                    message
                }
            }
        }
    }
}