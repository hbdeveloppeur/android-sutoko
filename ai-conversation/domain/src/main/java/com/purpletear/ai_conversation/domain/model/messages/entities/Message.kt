package com.purpletear.ai_conversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.interfaces.ConversationItem
import java.util.UUID

@Keep
open class Message(
    override val id: String = UUID.randomUUID().toString(),
    open var isAcknowledged: Boolean = false,
    open val hiddenState: MessageState,
    open val state: MessageState,
    open val role: MessageRole,
    open val typing: MessageTyping,
    open val aiCharacterId: Int?,
    open val timestamp: Long,
) : ConversationItem {

    fun acknowledge() {
        isAcknowledged = true
    }

    override fun equals(other: Any?): Boolean {
        return other is Message && id == other.id
                && state.code == other.state.code
                && role.code == other.role.code
                && typing == other.typing
                && aiCharacterId == other.aiCharacterId
                && timestamp == other.timestamp
                && isAcknowledged == other.isAcknowledged
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + typing.hashCode()
        result = 31 * result + (aiCharacterId ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isAcknowledged.hashCode()
        return result
    }

    fun copy(
        state: MessageState = this.state,
        hiddenState: MessageState = this.hiddenState,
        role: MessageRole = this.role,
        typing: MessageTyping = this.typing,
        aiCharacterId: Int? = this.aiCharacterId,
        timestamp: Long = this.timestamp,
        description: String? = null
    ): Message {
        return when (this) {
            is MessageText -> {
                this.copy(
                    id = id,
                    state = state,
                    hiddenState = hiddenState,
                    role = role,
                    typing = typing,
                    aiCharacterId = aiCharacterId,
                    timestamp = timestamp
                )
            }

            is MessageImage -> {
                this.copy(
                    id = id,
                    state = state,
                    hiddenState = hiddenState,
                    role = role,
                    aiCharacterId = aiCharacterId,
                    timestamp = timestamp,
                    description = description
                )
            }

            is MessageNarration -> {
                this.copy(
                    id = id,
                    timestamp = timestamp,
                    hiddenState = hiddenState,
                )
            }

            is MessageStoryChoiceGroup -> {
                this.copy(
                    id = id,
                    timestamp = timestamp,
                )
            }

            is MessageVocal -> {
                this.copy(
                    id = id,
                    state = state,
                    hiddenState = hiddenState,
                )
            }

            is MessageInviteCharacters -> {
                this.copy(
                    id = id,
                    timestamp = timestamp,
                )
            }


            else -> throw UnsupportedOperationException()
        }
    }
}
