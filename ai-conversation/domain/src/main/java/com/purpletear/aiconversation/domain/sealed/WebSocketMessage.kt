package com.purpletear.aiconversation.domain.sealed

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.CharacterStatus
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.messages.entities.Message

@Keep
sealed class WebSocketMessage {
    data object Ping : WebSocketMessage()
    data object Error : WebSocketMessage()
    data object AuthenticateSuccess : WebSocketMessage()
    data object AuthenticateFailure : WebSocketMessage()
    data object Seen : WebSocketMessage()
    data object Typing : WebSocketMessage()
    data object Block : WebSocketMessage()
    data class CharacterNewStatus(val status: CharacterStatus) : WebSocketMessage()
    data class BackgroundImageUpdate(val url: String) : WebSocketMessage()
    data class ConversationModeUpdate(val mode: ConversationMode) : WebSocketMessage()
    data object StopTyping : WebSocketMessage()
    data class ChatMessage(val message: Message) : WebSocketMessage()
    data class ErrorCode(val exception: Exception) : WebSocketMessage()
    data class MessagesAck(val serialIds: List<String>) : WebSocketMessage()
    data class InviteCharacters(val characters: List<AiCharacter>) : WebSocketMessage()
}