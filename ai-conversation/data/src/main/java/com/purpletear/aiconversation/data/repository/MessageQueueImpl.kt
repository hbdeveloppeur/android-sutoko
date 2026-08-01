package com.purpletear.aiconversation.data.repository

import android.util.Log
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.repository.MessageQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import purpletear.fr.purpleteartools.DelayHandler

class MessageQueueImpl() : MessageQueue {
    private companion object {
        const val TAG = "AiConv"
    }

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
        Log.d(TAG, "Queue: acknowledge ids=$ids (queue size=${_mutableQueue.value.size})")
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
        Log.d(TAG, "Queue: timer canceled")
        delayHandler.stop(waiterName)
    }

    override fun startTimer(onTick: (messages: List<Message>) -> Unit) {
        Log.d(TAG, "Queue: timer started (${waiterDuration}ms, queue size=${_mutableQueue.value.size})")
        delayHandler.stop(waiterName)
        delayHandler.operation(waiterName, waiterDuration) {
            Log.d(TAG, "Queue: timer fired, queue size=${_mutableQueue.value.size}")
            if (_mutableQueue.value.isNotEmpty()) {
                onTick(_mutableQueue.value.toList())
            }
        }
    }

    override fun add(message: Message) {
        Log.d(TAG, "Queue: add id=${message.id}")
        _mutableQueue.update {
            val list = it.toMutableList()
            list.add(message)
            Log.d(TAG, "Queue: size after add=${list.size}")
            list
        }
    }

    override fun remove(predicate: (Message) -> Boolean) {
        _mutableQueue.update {
            val list = it.toMutableList()
            list.removeIf(predicate)
            Log.d(TAG, "Queue: size after remove=${list.size} (was ${it.size})")
            list
        }
    }

    override fun clear() {
        Log.d(TAG, "Queue: cleared")
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
        Log.d(TAG, "Queue: mark state=$state (queue size=${_mutableQueue.value.size})")
        _mutableQueue.update { messages ->
            messages.map { message ->
                if (message.hiddenState !in setOf(MessageState.Sent)) {
                    message.copy(hiddenState = state)
                } else {
                    message
                }
            }
        }
    }
}