package com.purpletear.aiconversation.domain.model.messages.entities

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.interfaces.ConversationItem
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

    // No custom equals/hashCode here: subclasses are data classes and generate their own
    // structural equality from their constructor properties. A base-class implementation
    // comparing different fields would break substitutability (Liskov).

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
                    description = description ?: this.description
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
