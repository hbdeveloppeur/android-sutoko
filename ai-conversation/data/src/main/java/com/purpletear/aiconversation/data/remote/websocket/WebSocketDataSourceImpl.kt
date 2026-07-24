package com.purpletear.aiconversation.data.remote.websocket

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.purpletear.aiconversation.data.BuildConfig
import com.purpletear.aiconversation.data.exception.UnableToReachServiceException
import com.purpletear.aiconversation.data.remote.dto.TextWithId
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.WebSocketMessageType
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageNarration
import com.purpletear.aiconversation.domain.model.messages.entities.MessageText
import com.purpletear.aiconversation.domain.parser.WebsocketMessageParser
import com.purpletear.aiconversation.domain.repository.WebSocketDataSource
import com.purpletear.aiconversation.domain.sealed.WebSocketMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import purpletear.fr.purpleteartools.Language
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

class WebSocketDataSourceImpl(
    private val webSocketRequest: Request,
    private val websocketMessageParser: WebsocketMessageParser,
    private val authTimeoutMs: Long = DEFAULT_AUTH_TIMEOUT_MS,
) : WebSocketDataSource {
    // pingInterval: detect silently dead connections (radio switch, server crash
    // without FIN) so onFailure fires and the caller's reconnect logic engages.
    private val client = OkHttpClient.Builder()
        .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
        .build()

    private companion object {
        const val DEFAULT_AUTH_TIMEOUT_MS = 10_000L
        const val PING_INTERVAL_SECONDS = 20L
    }

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
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
                "texts" to messages.mapNotNull {
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
            val socket = webSocket
            if (socket == null) {
                emit(Result.failure(UnableToReachServiceException()))
                return@flow
            }
            if (!socket.send(Gson().toJson(data))) {
                emit(Result.failure(UnableToReachServiceException()))
                return@flow
            }
            emit(Result.success(Unit))
        }

    override fun connect(uid: String, token: String): Flow<WebSocketMessage> = callbackFlow {
        // A fresh handshake starts unauthenticated.
        _isConnected = false

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

                try {
                when (action) {

                    WebSocketMessageType.CONNECTED -> {
                        // Server handshake frame sent on TCP connect, before authentication.
                    }

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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)

                trySend(WebSocketMessage.Error)
                _isConnected = false
                close()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                _isConnected = false
                // A close must not end the stream silently: surface it as an error
                // so the caller's bounded reconnect logic engages.
                trySend(WebSocketMessage.Error)
                close()
            }
        }

        webSocket?.close(1000, "Reconnecting")
        this@WebSocketDataSourceImpl.webSocket = client.newWebSocket(webSocketRequest, listener)

        // Bounded handshake: if the server never completes authentication, surface an
        // error so the caller's reconnect logic engages instead of loading forever.
        val authTimeout = launch {
            delay(authTimeoutMs)
            if (!_isConnected) {
                trySend(WebSocketMessage.Error)
            }
        }

        awaitClose {
            authTimeout.cancel()
            // The OkHttpClient is shared by this @Singleton data source: only close the
            // socket. Shutting down the dispatcher would kill the client for every
            // future reconnection until process death.
            webSocket?.close(1000, "Flow closed")
            webSocket = null
        }
    }.catch {
        emit(WebSocketMessage.Error)
    }

    override fun send(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    override fun sendPong(uid: String, token: String) {
        val data = mapOf(
            "action" to "pong",
            "uid" to uid,
            "token" to token
        )
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
                throw WebsocketMessageParserException("The parsed JSON is not an object.")
            }
            val jsonObject = jsonElement.asJsonObject
            val str = jsonObject["action"]?.asString
                ?: throw WebsocketMessageParserException("Le JSON ne contient pas le champ 'action'")
            return WebSocketMessageType.entries.firstOrNull { it.code == str }
                ?: throw WebsocketMessageParserException("Unknown websocket action: $str")
        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException("Entrée non valide, ce n'est pas un JSON valide.")
        }
    }
}