package com.purpletear.ai_conversation.data.remote.websocket

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.purpletear.ai_conversation.data.BuildConfig
import com.purpletear.ai_conversation.data.remote.dto.TextWithId
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.WebSocketMessageType
import com.purpletear.ai_conversation.domain.exception.WebsocketMessageParserException
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageNarration
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageText
import com.purpletear.ai_conversation.domain.parser.WebsocketMessageParser
import com.purpletear.ai_conversation.domain.repository.WebSocketDataSource
import com.purpletear.ai_conversation.domain.sealed.WebSocketMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import purpletear.fr.purpleteartools.Language
import java.util.TimeZone
import java.util.UUID

class WebSocketDataSourceImpl(
    private val webSocketRequest: Request,
    private val websocketMessageParser: WebsocketMessageParser,
) : WebSocketDataSource {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private var _isConnected: Boolean = false
    override val isConnected: Boolean get() = _isConnected

    override fun sendMessage(
        characterId: Int,
        messages: List<Message>,
        userId: String,
        token: String,
        userName: String?,
    ): Flow<Result<Unit>> =
        flow {
            val data = mapOf(
                "action" to "send_message",
                "characterId" to characterId,
                "mrpId" to UUID.randomUUID().toString(),
                "uid" to userId,
                "token" to token,
                "appVersion" to BuildConfig.VERSION_NAME,
                "texts" to messages.map {
                    when (it) {
                        is MessageText -> TextWithId(it.id, it.text, MessageRole.User.code)
                        is MessageNarration -> TextWithId(
                            it.id,
                            it.text,
                            MessageRole.Narrator.code
                        )

                        else -> null
                    }
                },
                "timeZoneId" to TimeZone.getDefault().id,
                "langCode" to (Language.determineLangDirectory().take(2)),
                "userName" to userName,
            )
            webSocket?.send(
                Gson().toJson(data)
            )
            emit(Result.success(Unit))
        }

    override fun connect(uid: String, token: String): Flow<WebSocketMessage> = callbackFlow {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                this@WebSocketDataSourceImpl.authenticate(
                    webSocket = webSocket,
                    uid = uid,
                    token = token
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                if (text.contains("new_message_image")) {
                    print(text)
                }

                val action: WebSocketMessageType
                try {
                    action = extractActionFromJson(text)
                } catch (e: WebsocketMessageParserException) {
                    e.printStackTrace()
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }

                when (action) {

                    WebSocketMessageType.ERROR_CODE -> {
                        trySend(
                            WebSocketMessage.ErrorCode(
                                websocketMessageParser.parseError(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.MESSAGE_ACK -> {
                        trySend(
                            WebSocketMessage.MessagesAck(
                                websocketMessageParser.parseMessagesAck(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.CHAT_MESSAGE -> {
                        trySend(
                            WebSocketMessage.ChatMessage(
                                websocketMessageParser.parseMessage(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.INVITE_CHARACTERS -> {
                        trySend(
                            WebSocketMessage.InviteCharacters(
                                websocketMessageParser.parseCharacters(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.NEW_CHARACTER_STATUS -> {
                        trySend(
                            WebSocketMessage.CharacterNewStatus(
                                websocketMessageParser.parseCharacterStatus(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.NEW_BACKGROUND_IMAGE -> {
                        trySend(
                            WebSocketMessage.BackgroundImageUpdate(
                                websocketMessageParser.parseBackgroundImageUpdateUrl(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.STORY_CHOICE -> {
                        trySend(
                            WebSocketMessage.ChatMessage(
                                websocketMessageParser.parseStoryChoice(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.CHAT_NARRATION -> {
                        trySend(
                            WebSocketMessage.ChatMessage(
                                websocketMessageParser.parseMessage(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.CHAT_MESSAGE_IMAGE -> {
                        trySend(
                            WebSocketMessage.ChatMessage(
                                websocketMessageParser.parseImageMessage(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.CONVERSATION_MODE_UPDATE -> {
                        trySend(
                            WebSocketMessage.ConversationModeUpdate(
                                websocketMessageParser.parseConversationMode(
                                    text
                                )
                            )
                        )
                    }

                    WebSocketMessageType.ERROR -> {
                        trySend(WebSocketMessage.Error)
                    }

                    WebSocketMessageType.BLOCK -> {
                        trySend(WebSocketMessage.Block)
                    }

                    WebSocketMessageType.PING -> {
                        trySend(WebSocketMessage.Ping)
                    }

                    WebSocketMessageType.SEEN -> {
                        trySend(WebSocketMessage.Seen)
                    }

                    WebSocketMessageType.TYPING -> {
                        trySend(WebSocketMessage.Typing)
                    }


                    WebSocketMessageType.STOP_TYPING -> {
                        trySend(WebSocketMessage.StopTyping)
                    }

                    WebSocketMessageType.AUTHENTICATION_SUCCESS -> {
                        _isConnected = true
                        trySend(WebSocketMessage.AuthenticateSuccess)
                    }

                    WebSocketMessageType.AUTHENTICATION_FAILURE -> {
                        trySend(WebSocketMessage.AuthenticateFailure)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)

                trySend(WebSocketMessage.Error)
                _isConnected = false
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                close()
                _isConnected = false
            }
        }

        this@WebSocketDataSourceImpl.webSocket = client.newWebSocket(webSocketRequest, listener)

        awaitClose {
            webSocket?.close(1000, "Flow closed")
            webSocket = null
        }
    }.catch {
        emit(WebSocketMessage.Error)
    }

    override fun send(message: String): Boolean {
        return true
    }

    override fun sendPong(uid: String, token: String) {
        val data = mapOf(
            "action" to "pong",
            "uid" to uid,
            "token" to token
        );
        send(Gson().toJson(data))
    }


    private fun authenticate(webSocket: WebSocket, token: String, uid: String) {
        val data = mapOf(
            "action" to "authenticate",
            "token" to token,
            "uid" to uid
        )
        webSocket.send(
            Gson().toJson(data)
        )
    }

    /**
     * @throws WebsocketMessageParserException
     */
    private fun extractActionFromJson(json: String): WebSocketMessageType {
        try {
            val jsonElement = JsonParser.parseString(json)
            if (!jsonElement.isJsonObject) {
                // Log the problematic string for debugging
                println("Parsed JSON is not an object: $json")
                throw WebsocketMessageParserException("The parsed JSON is not an object.")
            }
            val jsonObject = jsonElement.asJsonObject
            val str = jsonObject["action"]?.asString
                ?: throw WebsocketMessageParserException("Le JSON ne contient pas le champ 'action'")
            return WebSocketMessageType.entries.first { it.code == str }
        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException("Entrée non valide, ce n'est pas un JSON valide.")
        }
    }
}