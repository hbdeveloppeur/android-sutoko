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
    @Keep
    data class CharacterNewStatus(val status: CharacterStatus) : WebSocketMessage()
    @Keep
    data class BackgroundImageUpdate(val url: String) : WebSocketMessage()
    @Keep
    data class ConversationModeUpdate(val mode: ConversationMode) : WebSocketMessage()
    data object StopTyping : WebSocketMessage()
    @Keep
    data class ChatMessage(val message: Message) : WebSocketMessage()
    @Keep
    data class ErrorCode(val exception: Exception) : WebSocketMessage()
    @Keep
    data class MessagesAck(val serialIds: List<String>) : WebSocketMessage()
    @Keep
    data class InviteCharacters(val characters: List<AiCharacter>) : WebSocketMessage()
}