package com.purpletear.aiconversation.data.repository

import com.purpletear.aiconversation.data.BuildConfig
import com.purpletear.aiconversation.data.exception.NoResponseException
import com.purpletear.aiconversation.data.remote.MessageApi
import com.purpletear.aiconversation.data.remote.dto.toDomain
import com.purpletear.aiconversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.Conversation
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.aiconversation.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import purpletear.fr.purpleteartools.Language
import java.util.TimeZone

class ConversationRepositoryImpl(private val api: MessageApi) : ConversationRepository {

    private val _messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    private val messagesFlow: StateFlow<List<Message>> get() = _messagesFlow


    private var _conversationSettings: MutableStateFlow<Conversation?> = MutableStateFlow(null)
    private val conversationSettings: StateFlow<Conversation?> get() = _conversationSettings


    override fun clear() {
        _messagesFlow.update { emptyList() }
        _conversationSettings.update { null }
    }

    override suspend fun restartConversation(
        userId: String,
        aiCharacterId: Int
    ): Flow<Result<Unit>> = flow {
        val apiResponse = api.restart(
            userId = userId,
            characterId = aiCharacterId,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            clearMessages()
            clearSettings()
            emit(Result.success(Unit))
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
            return@flow
        }
    }.catch {
        emit(Result.failure(it))
    }

    private fun clearMessages() {
        _messagesFlow.update { emptyList() }
    }

    override suspend fun saveForFineTuning(userId: String): Flow<Result<Unit>> = flow {
        val apiResponse = api.saveForFineTune(
            userId = userId,
        )
        kotlinx.coroutines.delay(1200)

        if (apiResponse.isSuccessful) {
            emit(Result.success(Unit))
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
            return@flow
        }
    }.catch {
        emit(Result.failure(it))
    }

    private fun clearSettings() {
        _conversationSettings.update { _conversationSettings.value?.reset() }
    }

    override suspend fun getMessagesStream(
        userId: String?,
        aiCharacterId: Int,
        userToken: String?
    ): Flow<List<Message>> {
        try {
            val apiResponse = api.getAll(
                userId = userId,
                characterId = aiCharacterId,
                token = userToken,
                appVersion = BuildConfig.VERSION_NAME
            )

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { messages ->
                    addMessages(messages)
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                exception.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return messagesFlow
    }


    override suspend fun getSettings(
        userId: String?,
        aiCharacterId: Int
    ): Flow<Conversation?> {
        val apiResponse = api.getSettings(
            userId = userId,
            characterId = aiCharacterId,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { conversationSettings ->
                _conversationSettings.value = conversationSettings.toDomain()
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            exception.printStackTrace()
        }

        return conversationSettings
    }

    private fun getTimeZoneCode(): String {
        val timeZone = TimeZone.getDefault()
        return timeZone.id
    }

    override suspend fun sendMessageMedia(
        userId: String,
        token: String,
        aiCharacterId: Int,
        userName: String?,
        mediaId: Int,
        role: MessageRole,
    ): Flow<Result<Unit>> = flow {

        val apiResponse = api.sendMessageImage(
            userId = userId,
            token = token,
            aiCharacterId = aiCharacterId,
            mediaId = mediaId,
            role = role.code,
            langCode = (Language.determineLangDirectory()
                .take(2)),
            timeZoneId = getTimeZoneCode(),
            userName = userName,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let {
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            exception.printStackTrace()
            emit(Result.failure(NoResponseException()))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override fun updateMessage(message: Message, update: (Message) -> Message) {
        _messagesFlow.update { currentList ->
            val updatedList = currentList.toMutableList()
            val index = updatedList.indexOfFirst { it.id == message.id }
            if (index != -1) {
                updatedList[index] = update(updatedList[index])
            }
            updatedList
        }
    }

    override fun addMessage(message: Message) {
        val newMap = mutableMapOf(message.id to message)
        val map = _messagesFlow.value.associateBy { it.id }.toMutableMap()
        newMap.putAll(map)
        _messagesFlow.value = newMap.values.toMutableList()
    }

    private fun addMessages(messages: List<Message>) {
        val newMap = messages.associateBy { it.id }.toMutableMap()
        val map = _messagesFlow.value.associateBy { it.id }.toMutableMap()
        newMap.putAll(map)
        _messagesFlow.value = newMap.values.toMutableList()
    }

    override fun mark(messages: List<Message>, state: MessageState) {
        _messagesFlow.update { currentList ->
            val updatedList = currentList.toMutableList()
            messages.forEach { message ->
                val index = updatedList.indexOfFirst { it.id == message.id }
                if (index != -1 && updatedList[index].state != state) {
                    updatedList[index] = updatedList[index].copy(state = state)
                }
            }
            updatedList
        }
    }

    override fun selectChoice(
        choiceGroup: MessageStoryChoiceGroup,
        choice: MessageStoryChoice
    ): Flow<Result<Unit>> = flow {
        _messagesFlow.update { currentList ->
            val updatedList = currentList.toMutableList()
            val index = updatedList.indexOfFirst { it.id == choiceGroup.id }

            if (index != -1) {
                val t = updatedList[index]
                if (t is MessageStoryChoiceGroup) {
                    val choices = t.choices.toMutableList()
                    choices.forEachIndexed { j, c ->
                        choices[j] = c.copy(isSelected = c.id == choice.id)
                    }
                    updatedList[index] = t.copy(choices = choices, isConsumed = true)
                }
            }
            updatedList
        }
    }
}