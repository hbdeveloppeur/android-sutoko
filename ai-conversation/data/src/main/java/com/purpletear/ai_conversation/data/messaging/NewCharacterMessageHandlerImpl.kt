package com.purpletear.ai_conversation.data.messaging

import com.purpletear.ai_conversation.data.exception.MalformedResponseException
import com.purpletear.ai_conversation.data.exception.UnknownActionException
import com.purpletear.ai_conversation.domain.messaging.NewCharacterMessageHandler
import com.purpletear.core.remote.Server
import com.purpletear.sutoko.notification.repository.NotificationRepository
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.SendNotificationUseCase

class NewCharacterMessageHandlerImpl(
    private val notificationRepository: NotificationRepository,
) : NewCharacterMessageHandler {

    /**
     * @throws UnknownActionException
     * @throws MalformedResponseException
     */
    override suspend fun handleMessage(data: Map<String, String>) {
        val title = data["title"] ?: throw MalformedResponseException()
        val body = data["body"] ?: throw MalformedResponseException()
        val characterId = data["character_id"] ?: throw MalformedResponseException()
        val imageUrl = data["imageUrl"]

        SendNotificationUseCase(notificationRepository).invoke(
            title = title,
            message = body,
            imageUrl = imageUrl?.let { getRemoteAssetsUrl(it) },
            destination = "conversation/$characterId",
            screen = Screen.Conversation(characterId.toInt())
        )
    }


    fun getRemoteAssetsUrl(url: String): String {
        if (url.contains("http") || url.contains(".com")) return url
        return "${Server.urlPrefix()}/$url".replace("//", "/")
    }
}