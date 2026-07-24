package com.purpletear.aiconversation.data.repository

import com.purpletear.aiconversation.data.remote.MessageApi
import com.purpletear.aiconversation.data.remote.dto.ConversationDto
import com.purpletear.aiconversation.data.remote.dto.SendMessageResponseDto
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageText
import com.purpletear.aiconversation.domain.model.messages.entities.MessageTyping
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

/**
 * The message list contract is newest-first: index 0 is the most recent message,
 * rendered at the bottom of the screen (LazyColumn with reverseLayout = true).
 * A locally sent message must therefore appear at index 0 immediately.
 */
class ConversationRepositoryImplTest {

    private class FakeMessageApi(
        private val serverMessages: List<Message> = emptyList()
    ) : MessageApi {
        override suspend fun getAll(
            userId: String?,
            token: String?,
            characterId: Int,
            appVersion: String
        ): Response<List<Message>> = Response.success(serverMessages)

        override suspend fun sendMessageImage(
            userId: String?, token: String?, aiCharacterId: Int, mediaId: Int,
            role: String, userName: String?, timeZoneId: String, langCode: String,
            appVersion: String
        ): Response<Unit> = throw UnsupportedOperationException()

        override suspend fun saveForFineTune(userId: String?): Response<Unit> =
            throw UnsupportedOperationException()

        override suspend fun getSettings(
            userId: String?, aiCharacterId: Int, appVersion: String
        ): Response<ConversationDto> = throw UnsupportedOperationException()

        override suspend fun restart(
            userId: String?, aiCharacterId: Int, appVersion: String
        ): Response<Unit> = throw UnsupportedOperationException()

        override suspend fun sendMessage(
            mrpId: RequestBody, userId: RequestBody, token: RequestBody,
            aiCharacterCode: RequestBody, appVersion: RequestBody, langCode: RequestBody,
            timeZoneId: RequestBody, userName: RequestBody?,
            texts: List<MultipartBody.Part>, audioFiles: List<MultipartBody.Part>
        ): Response<SendMessageResponseDto> = throw UnsupportedOperationException()
    }

    private fun message(id: String, timestamp: Long): Message = MessageText(
        text = "text-$id",
        id = id,
        state = MessageState.Sent,
        hiddenState = MessageState.Idle,
        role = MessageRole.User,
        typing = MessageTyping(durationMs = 0, delayMs = 0),
        aiCharacterId = 1,
        timestamp = timestamp
    )

    @Test
    fun `addMessage inserts the new message at index 0 of the newest-first list`() = runTest {
        val repository = ConversationRepositoryImpl(
            FakeMessageApi(
                serverMessages = listOf(
                    message(id = "server-newest", timestamp = 2_000),
                    message(id = "server-oldest", timestamp = 1_000),
                )
            )
        )

        val stream = repository.getMessagesStream(
            userId = null, aiCharacterId = 1, userToken = null
        ) as StateFlow<List<Message>>
        assertEquals(listOf("server-newest", "server-oldest"), stream.value.map { it.id })

        repository.addMessage(message(id = "local", timestamp = 3_000))

        assertEquals(
            listOf("local", "server-newest", "server-oldest"),
            stream.value.map { it.id }
        )
    }

    @Test
    fun `addMessage keeps the latest message at index 0 and never duplicates ids`() = runTest {
        val repository = ConversationRepositoryImpl(FakeMessageApi())
        val stream = repository.getMessagesStream(
            userId = null, aiCharacterId = 1, userToken = null
        ) as StateFlow<List<Message>>

        val userMessage = message(id = "user", timestamp = 1_000)
        repository.addMessage(userMessage)
        repository.addMessage(message(id = "assistant-reply", timestamp = 2_000))
        repository.addMessage(userMessage)

        assertEquals(
            listOf("user", "assistant-reply"),
            stream.value.map { it.id }
        )
    }
}
