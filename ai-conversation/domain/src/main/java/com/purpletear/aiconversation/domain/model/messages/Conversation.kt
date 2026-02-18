package com.purpletear.aiconversation.domain.model.messages

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.model.AiCharacter
import javax.annotation.concurrent.Immutable

@Keep
@Immutable
data class Conversation(
    val id: String?,
    val minAppCode: Int,
    val isBlocked: Boolean,
    val mode: ConversationMode,
    val startingBackgroundUrl: String?,
    val character: AiCharacter,
    val characters: List<AiCharacter>,
) {
    fun reset(): Conversation {
        return this.copy(
            id = null,
            isBlocked = false,
            mode = ConversationMode.Sms,
            startingBackgroundUrl = null,
            characters = listOf(character)
        )
    }
}



