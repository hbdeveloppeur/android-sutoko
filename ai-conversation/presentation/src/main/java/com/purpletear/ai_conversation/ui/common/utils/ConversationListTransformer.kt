package com.purpletear.ai_conversation.ui.common.utils

import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.ui.component.blurred_message.MessagePositionInGroup
import com.purpletear.ai_conversation.ui.model.UIMessage

private fun isLastReceivedMessage(messages: List<Message>, index: Int): Boolean {
    messages.forEachIndexed { i1, it ->
        if (it.role == MessageRole.Assistant) {
            return i1 == index
        }
    }
    return false
}

fun conversationListTransformer(messages: List<Message>, index: Int, current: Message): UIMessage {
    fun isGrouped(a: Message?, b: Message?): Boolean {
        return a?.role == b?.role && a?.aiCharacterId == b?.aiCharacterId
    }

    fun getShape(previous: Message?, next: Message?): MessagePositionInGroup {
        return when (true) {
            (previous == null && next == null) -> MessagePositionInGroup.PositionSingle
            (isGrouped(previous, current) && isGrouped(
                current,
                next
            )) -> MessagePositionInGroup.PositionMiddle

            (isGrouped(previous, current)) -> MessagePositionInGroup.PositionFirst
            (isGrouped(current, next)) -> MessagePositionInGroup.PositionLast
            else -> MessagePositionInGroup.PositionMiddle
        }
    }

    fun getOrNull(index: Int): Message? {
        return try {
            messages[index]
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    return UIMessage(
        message = current,
        shape = getShape(getOrNull(index = index - 1), getOrNull(index = index + 1)),
        displaysDate = isLastReceivedMessage(messages, index = index)
    )
}