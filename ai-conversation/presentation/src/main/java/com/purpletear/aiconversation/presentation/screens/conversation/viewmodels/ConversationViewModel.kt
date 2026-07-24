package com.purpletear.aiconversation.presentation.screens.conversation.viewmodels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.data.exception.NotEnoughCoinsException
import com.purpletear.aiconversation.data.exception.UserNameNotFoundException
import com.purpletear.aiconversation.domain.enums.CharacterStatus
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.Media
import com.purpletear.aiconversation.domain.model.messages.Conversation
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageImage
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.aiconversation.domain.repository.MessageQueue
import com.purpletear.aiconversation.domain.repository.WebSocketDataSource
import com.purpletear.aiconversation.domain.sealed.WebSocketMessage
import com.purpletear.aiconversation.domain.usecase.AddMessageToConversationUseCase
import com.purpletear.aiconversation.domain.usecase.ClearConversationRepositoryUseCase
import com.purpletear.aiconversation.domain.usecase.DescribeMediaUseCase
import com.purpletear.aiconversation.domain.usecase.GetAiTokensStateUseCase
import com.purpletear.aiconversation.domain.usecase.GetAvatarAndBannerPairUseCase
import com.purpletear.aiconversation.domain.usecase.GetCharacterStatusUseCase
import com.purpletear.aiconversation.domain.usecase.GetConversationMessagesUseCase
import com.purpletear.aiconversation.domain.usecase.GetConversationSettingsUseCase
import com.purpletear.aiconversation.domain.usecase.MakeStoryChoiceUseCase
import com.purpletear.aiconversation.domain.usecase.RestartConversationUseCase
import com.purpletear.aiconversation.domain.usecase.SaveForFineTuningUseCase
import com.purpletear.aiconversation.domain.usecase.SelectMessageChoice
import com.purpletear.aiconversation.domain.usecase.SendMessageMediaUseCase
import com.purpletear.aiconversation.domain.usecase.SendMessageUseCase
import com.purpletear.aiconversation.domain.usecase.TransformTextToUserMessageUseCase
import com.purpletear.aiconversation.domain.usecase.TransformUrlToMessageImageUseCase
import com.purpletear.aiconversation.domain.usecase.UpdateConversationMessagesState
import com.purpletear.aiconversation.domain.usecase.UpdateDeviceTokenUseCase
import com.purpletear.aiconversation.domain.usecase.UpdateMessageToConversationUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.conversationListTransformer
import com.purpletear.aiconversation.presentation.model.UIMessage
import com.purpletear.aiconversation.presentation.sealed.AlertState
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.permission.domain.sealed.Permission
import com.purpletear.sutoko.permission.domain.usecase.AskPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.EditTextPopUp
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val updateMessageToConversationUseCase: UpdateMessageToConversationUseCase,
    private val transformUrlToMessageImageUseCase: TransformUrlToMessageImageUseCase,
    private val addMessageToConversationUseCase: AddMessageToConversationUseCase,
    private val updateConversationMessagesState: UpdateConversationMessagesState,
    private val getConversationSettingsUseCase: GetConversationSettingsUseCase,
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val transformTextToMessageUseCase: TransformTextToUserMessageUseCase,
    private val getAvatarAndBannerPairUseCase: GetAvatarAndBannerPairUseCase,
    private val restartConversationUseCase: RestartConversationUseCase,
    private val userRepository: UserRepository,
    private val getAiTokensStateUseCase: GetAiTokensStateUseCase,
    private val getCharacterStatusUseCase: GetCharacterStatusUseCase,
    private val popUpInteractionUseCase: GetPopUpInteractionUseCase,
    private val saveForFineTuningUseCase: SaveForFineTuningUseCase,
    private val sendMessageImageUseCase: SendMessageMediaUseCase,
    private val makeStoryChoiceUseCase: MakeStoryChoiceUseCase,
    private val describeMediaUseCase: DescribeMediaUseCase,
    private val selectMessageChoice: SelectMessageChoice,
    private val webSocketDataSource: WebSocketDataSource,
    private val sendMessageUseCase: SendMessageUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val messageQueue: MessageQueue,
    private val clearConversationRepositoryUseCase: ClearConversationRepositoryUseCase,
    private val toastService: MakeToastService,
    private val askPermissionUseCase: AskPermissionUseCase,
    private val updateDeviceTokenUseCase: UpdateDeviceTokenUseCase,

    ) : ViewModel() {


    private var _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var _characters = mutableMapOf<Int, AiCharacter>()
    val characters: Map<Int, AiCharacter>
        get() = _characters

    // Tools view is opened
    private var _toolsViewIsOpened: MutableState<Boolean> = mutableStateOf(false)
    val toolsViewIsOpened: State<Boolean>
        get() = _toolsViewIsOpened

    private var _settingsViewIsOpened: MutableState<Boolean> = mutableStateOf(false)
    val settingsViewIsOpened: State<Boolean>
        get() = _settingsViewIsOpened

    private var _editTextMessage: MutableState<String> = mutableStateOf("")
    val editTextMessage: State<String>
        get() = _editTextMessage

    private var _userCoinsCount: MutableState<Int?> = mutableStateOf(null)
    val userCoinsCount: State<Int?>
        get() = _userCoinsCount


    private var _characterStatus: MutableState<CharacterStatus?> =
        mutableStateOf(null)
    val characterStatus: State<CharacterStatus?>
        get() = _characterStatus

    private var _isTyping: MutableState<Boolean> = mutableStateOf(false)
    val isTyping: State<Boolean>
        get() = _isTyping

    private var aiCharacterId: Int = 1


    private var _conversationSettings: MutableState<Conversation?> = mutableStateOf(
        null
    )

    val conversationSettings: State<Conversation?>
        get() = _conversationSettings


    private var _alert: MutableState<AlertState?> = mutableStateOf(
        null
    )
    val alert: State<AlertState?> get() = _alert

    private var _loadingDescriptionMessageId: MutableState<String?> = mutableStateOf(null)
    val loadingDescriptionMessageId: State<String?> get() = _loadingDescriptionMessageId

    private var _inviteCharacterPageIsOpened: MutableState<Boolean> = mutableStateOf(false)
    val inviteCharacterPageIsOpened: State<Boolean> get() = _inviteCharacterPageIsOpened

    private var updateMessagesStateJob: Job? = null
    private var webSocketJob: Job? = null

    var messages = mutableStateListOf<UIMessage>()

    init {
        clearConversationRepositoryUseCase()
        aiCharacterId = savedStateHandle.get<Int>("character_id") ?: 1
        viewModelScope.launch {
            getConversationSettings()
            streamMessages()
            getCharacterStatus()
            bindToWebSocket()
        }
        requestNotificationPermission()
        viewModelScope.launch {
            setUserDeviceToken()
        }
    }

    fun onResume() {
        viewModelScope.launch {
            updateUserCoinsCount()
            // Safety net: if the socket died while the screen was away (e.g. the flow
            // completed without an error), restore live updates without forcing the
            // user to leave and re-enter the page.
            if (webSocketBindRequested
                && webSocketReconnectAttempts == 0
                && _alert.value !is AlertState.ConnectionError
                && webSocketDataSource.isConnected.not()
            ) {
                bindToWebSocket()
            }
        }
    }

    private suspend fun setUserDeviceToken() {
        val user = userRepository.observeUser().first() ?: return
        executeFlowResultUseCase({
            updateDeviceTokenUseCase(user.id, user.token)
        })
    }

    private fun requestNotificationPermission() {
        askPermissionUseCase(Permission.Notification)
    }

    private fun requestName() {
        messageQueue.cancelTimer()
        val tag = showPopUpUseCase(
            EditTextPopUp(
                title = UiText.StringResource(R.string.ai_conversation_conversation_dialog_username_title),
                placeholder = UiText.StringResource(R.string.ai_conversation_conversation_dialog_username_placeholder),
            )
        )

        executeFlowUseCase({
            popUpInteractionUseCase(tag)
        }, onStream = { interaction ->
            if (interaction.event is PopUpUserInteraction.ConfirmText) {
                val messagesToInsert = messageQueue.messages
                messageQueue.cancelTimer()
                viewModelScope.launch {
                    sendMessage(
                        userName = (interaction.event as PopUpUserInteraction.ConfirmText).text,
                        messages = messagesToInsert.value,
                        onSuccess = {
                            messageQueue.remove { m -> m.id in messagesToInsert.value.map { s -> s.id } }
                        })
                }
            }
        }, onFailure = {
            Log.d("ConversationViewModel", "on Failure: ${it.message}")
        })
    }

    private fun onNoMoreCoins() {
        messageQueue.cancelTimer()
        val tag = showPopUpUseCase(
            SutokoPopUp(
                title = UiText.StringResource(R.string.ai_conversation_buy_coins),
                description = UiText.StringResource(R.string.ai_conversation_missing_coins),
                buttonText = UiText.StringResource(R.string.ai_conversation_continue),
                icon = PopUpIconDrawable(R.drawable.ai_conversation_presentation_item_coin),
                iconHeight = 32.dp,
            )
        )

        executeFlowUseCase({
            popUpInteractionUseCase(tag)
        }, onStream = {
            Log.d("ConversationViewModel", "on Stream: ${it.event}")
        })
    }

    internal suspend fun userIsModerator(): Boolean {
        val user = userRepository.observeUser().first() ?: return false
        return user.id == "8be954c7a18f4e7cba9c"
    }

    internal fun closeInviteCharacterPage() {
        _inviteCharacterPageIsOpened.value = false
    }

    internal fun closeSettingsPage() {
        _settingsViewIsOpened.value = false
    }

    internal fun openSettingsPage() {
        _settingsViewIsOpened.value = true
    }

    /**
     * Resets all overlay states to ensure clean navigation.
     * Called when the screen is being disposed or when back navigation occurs.
     */
    internal fun resetOverlayStates() {
        _inviteCharacterPageIsOpened.value = false
        _settingsViewIsOpened.value = false
        _toolsViewIsOpened.value = false
    }

    /**
     * Checks if any overlay is currently open.
     */
    internal fun hasOpenOverlays(): Boolean {
        return _inviteCharacterPageIsOpened.value ||
                _settingsViewIsOpened.value ||
                _toolsViewIsOpened.value
    }

    private var _isLoadingSavingForFineTuning: MutableState<Boolean> = mutableStateOf(false)
    val isLoadingSavingForFineTuning: State<Boolean> get() = _isLoadingSavingForFineTuning


    internal fun onClickSaveForFineTuning() {
        viewModelScope.launch {
            val user = userRepository.observeUser().first() ?: throw IllegalStateException()
            _isLoadingSavingForFineTuning.value = true
            executeFlowResultUseCase(
                useCase = { saveForFineTuningUseCase(user.id) },
                onSuccess = {
                    _isLoadingSavingForFineTuning.value = false
                },
                onFailure = {
                    _isLoadingSavingForFineTuning.value = false
                    Log.d("ConversationViewModel", "on Failure: ${it.message}")
                }
            )
        }
    }

    private suspend fun getConversationSettings() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        _isLoading.value = true
        executeFlowUseCase(
            useCase = {
                getConversationSettingsUseCase(user.id, aiCharacterId)
            },
            onStream = { conversation ->
                _conversationSettings.value = conversation
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }

    private suspend fun streamMessages() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        executeFlowUseCase(
            useCase = {
                getConversationMessagesUseCase(
                    user.id, aiCharacterId, user.token,
                )
            },
            onStream = { messages ->
                addOrSetMessagesToList(newMessages = messages)
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }

    internal fun onAlertClick(alertState: AlertState) {
        when (alertState) {
            is AlertState.CharacterBlockedUser -> {
                viewModelScope.launch {
                    restartConversation()
                }
            }

            AlertState.ConnectionError -> {
                _alert.value = null
                webSocketReconnectAttempts = 0
                viewModelScope.launch {
                    bindToWebSocket()
                }
            }
        }
    }

    private fun reset() {
        messageQueue.clear()
        _conversationSettings.value =
            _conversationSettings.value?.copy(isBlocked = false, mode = ConversationMode.Sms)
    }

    private suspend fun restartConversation() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        reset()
        _isLoading.value = true
        executeFlowResultUseCase(
            useCase = { restartConversationUseCase(user.id, aiCharacterId) },
            onSuccess = {
                _isLoading.value = false
                messages.clear()
                _alert.value = null
            },
            onFailure = {
                _isLoading.value = false
                Log.d("ConversationViewModel", "on Failure: ${it.message}")
            }
        )
    }

    private suspend fun getCharacterStatus() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        executeFlowResultUseCase(
            useCase = { getCharacterStatusUseCase(user.id, aiCharacterId) },
            onSuccess = {
                _characterStatus.value = it.state
            },
            onFailure = {
                Log.d("ConversationViewModel", "on Failure: ${it.message}")
            }
        )
    }

    internal fun onMessageEditTextChanged(text: String) {
        _editTextMessage.value = text
        if (messageQueue.isNotEmpty()) {
            startMessageQueueTimer()
        }
    }

    internal fun onClickAddCharacter() {
        _inviteCharacterPageIsOpened.value = true
        _toolsViewIsOpened.value = false
    }


    internal fun onClickToolsViewButton() {
        _toolsViewIsOpened.value = !_toolsViewIsOpened.value
    }


    internal fun closeToolsView() {
        _toolsViewIsOpened.value = false
    }

    internal fun onClickChoice(
        messageStoryChoice: MessageStoryChoice,
        messageStoryChoiceGroup: MessageStoryChoiceGroup
    ) {
        viewModelScope.launch {
            val user = userRepository.observeUser().first()
            if (null == user) {
                onUserNotConnected()
                return@launch
            }

            if (messageStoryChoiceGroup.isConsumed) {
                return@launch
            }

            executeFlowResultUseCase(
                useCase = { selectMessageChoice(messageStoryChoiceGroup, messageStoryChoice) },
                onSuccess = {

                },
                onFailure = {
                    Log.d("ConversationViewModel", "onFailure: ${it.message}")
                    // TODO :  Failure.
                }
            )

            executeFlowResultUseCase(
                useCase = { makeStoryChoiceUseCase(user.id, messageStoryChoice) },
                onSuccess = {

                },
                onFailure = {
                    Log.d("ConversationViewModel", "onFailure: ${it.message}")
                    // TODO :  Failure
                }
            )
        }
    }

    // TODO
    private fun onConversationSettingsLoaded(settings: Conversation) {
        _conversationSettings.value = settings
        if (settings.isBlocked) {
            blockConversation()
        }
    }

    private var webSocketReconnectAttempts: Int = 0
    private var webSocketBindRequested: Boolean = false

    private fun onUserNotConnected() {
        // TODO("show error message")
    }

    private fun onAuthenticateFailure() {
        // Retrying would fail again with the same token: stop the loader and surface the error.
        _isLoading.value = false
        _alert.value = AlertState.ConnectionError
    }

    fun bindNavigationChanges(savedStateHandle: SavedStateHandle?) {
        val imageRequestSerialId: String? = savedStateHandle?.get("imageRequestSerialId")
        imageRequestSerialId?.let {
            savedStateHandle["imageRequestSerialId"] = null
            viewModelScope.launch {
                insertImageInConversation(it)
            }
        }
    }

    private suspend fun insertImageInConversation(imageRequestSerialId: String) {
        if (messages.isEmpty()) {
            toastService(R.string.ai_conversation_error_insert_media_conversation_empty)
            return
        }

        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        executeFlowResultUseCase(
            useCase = { getAvatarAndBannerPairUseCase(imageRequestSerialId) },
            onSuccess = { pair ->
                pair.banner?.let { media ->
                    val message = transformUrlToMessageImageUseCase(
                        url = media.url,
                        role = MessageRole.Narrator
                    )

                    addMessageToConversationUseCase(message)

                    executeFlowResultUseCase(useCase = {
                        sendMessageImageUseCase(
                            userId = user.id,
                            token = user.token,
                            aiCharacterId = aiCharacterId,
                            userName = null,
                            mediaId = media.id,
                            role = message.role,
                        )
                    }, onSuccess = {

                    }, onFailure = {
                        // TODO : mark failure
                    })

                    if (message.description == null) {
                        viewModelScope.launch {
                            loadMediaDescription(message, media)
                        }
                    }
                }
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
                // Failure.
            }
        )
    }

    private suspend fun loadMediaDescription(message: MessageImage, media: Media) {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        _loadingDescriptionMessageId.value = message.id
        executeFlowResultUseCase(useCase = {
            describeMediaUseCase(
                mediaId = media.id,
                userId = user.id,
            )
        }, onSuccess = { description ->
            updateMessageToConversationUseCase(message) { message ->
                message.copy(description = description) as MessageImage
            }
            _loadingDescriptionMessageId.value = null
        }, onFailure = {
            _loadingDescriptionMessageId.value = null
        })
    }


    private suspend fun bindToWebSocket() {
        webSocketBindRequested = true
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        _isLoading.value = true
        webSocketJob?.cancel()
        webSocketJob = viewModelScope.launch(Dispatchers.IO) {
            webSocketDataSource.connect(
                uid = user.id,
                token = user.token
            ).collect { message ->
                withContext(Dispatchers.Main) {
                    handleWebSocketMessage(message)
                }
            }
        }
    }

    private fun onWebSocketError() {
        if (webSocketReconnectAttempts >= MAX_WEBSOCKET_RECONNECT_ATTEMPTS) {
            _isLoading.value = false
            _alert.value = AlertState.ConnectionError
            return
        }
        webSocketReconnectAttempts++
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            // Bounded backoff: 1s, 2s, 4s
            delay(1000L * (1 shl (webSocketReconnectAttempts - 1)))
            if (webSocketDataSource.isConnected.not()) {
                bindToWebSocket()
            }
        }
    }

    private fun acknowledgeMessages(ids: List<String>) {
        messageQueue.acknowledge(ids)
    }

    private suspend fun handleWebSocketMessage(it: WebSocketMessage) {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        when (it) {

            is WebSocketMessage.MessagesAck -> {
                acknowledgeMessages(it.serialIds)
            }

            is WebSocketMessage.ErrorCode -> {
                handleError(it.exception)
            }

            WebSocketMessage.AuthenticateFailure -> onAuthenticateFailure()
            WebSocketMessage.AuthenticateSuccess -> {
                webSocketReconnectAttempts = 0
                if (_alert.value is AlertState.ConnectionError) {
                    _alert.value = null
                }
                _isLoading.value = false
            }

            is WebSocketMessage.ChatMessage -> {
                onWebSocketMessage(it.message)
            }

            WebSocketMessage.Typing -> {
                setTypingMode(true)
            }

            WebSocketMessage.Block -> {
                blockConversation()
            }

            WebSocketMessage.StopTyping -> {
                setTypingMode(false)
            }

            WebSocketMessage.Ping -> {
                this.webSocketDataSource.sendPong(user.id, user.token)
            }

            WebSocketMessage.Seen -> {
                _characterStatus.value = CharacterStatus.Online
                setSeen()
            }

            WebSocketMessage.Error -> {
                onWebSocketError()
            }

            is WebSocketMessage.CharacterNewStatus -> {
                updateStatus(status = it.status)
            }

            is WebSocketMessage.ConversationModeUpdate -> {
                updateConversationMode(mode = it.mode)
            }

            is WebSocketMessage.InviteCharacters -> {
                onCharacterInvitedToConversation(characters = it.characters)
            }

            is WebSocketMessage.BackgroundImageUpdate -> {
                _conversationSettings.value = _conversationSettings.value?.copy(
                    startingBackgroundUrl = it.url
                )
            }

            else -> {
                Log.e("handleWebSocketMessage", "Unhandled message: $it")
            }
        }
    }

    private fun handleError(exception: Exception) {
        setTypingMode(false)
        when (exception) {
            is UserNameNotFoundException -> {
                setTypingMode(false)
                requestName()
                return
            }

            is NotEnoughCoinsException -> {
                onNoMoreCoins()
                return
            }

            else -> Log.e("ConversationViewModel", "Unhandled error: $exception")
        }
    }

    private fun blockConversation() {
        _conversationSettings.value = _conversationSettings.value?.copy(isBlocked = true)
        _conversationSettings.value?.character?.let { character ->
            _alert.value = AlertState.CharacterBlockedUser(character.firstName)
        }
    }

    private fun updateStatus(status: CharacterStatus) {
        _characterStatus.value = status
    }

    private fun updateConversationMode(mode: ConversationMode) {
        _conversationSettings.value = _conversationSettings.value?.copy(mode = mode)
    }

    private fun onCharacterInvitedToConversation(characters: List<AiCharacter>) {
        _characters.putAll(
            characters.associateBy { it.id }
        )
    }


    private fun setSeen() {
        // Mark last messages as seen
        updateConversationMessagesState(messages.map { it.message }, MessageState.Seen)
    }

    private fun setTypingMode(isTyping: Boolean) {
        this._isTyping.value = isTyping
    }


    private fun onWebSocketMessage(message: Message) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addMessageToConversationUseCase(message)
            }
        }
    }


    private fun addOrSetMessagesToList(newMessages: List<Message>) {
        messages.clear()
        if (newMessages.isNotEmpty()) {
            messages.addAll(newMessages.mapIndexed { index, item ->
                conversationListTransformer(
                    messages = newMessages,
                    index = index,
                    current = item
                )
            })
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        // viewModelScope is already cancelled here: flush the queue on a detached scope.
        val pending = messageQueue.messages.value.filter {
            it.hiddenState !in listOf(MessageState.Sending, MessageState.Sent) && !it.isAcknowledged
        }
        if (pending.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                val user = userRepository.observeUser().first() ?: return@launch
                sendMessageUseCase(
                    characterId = aiCharacterId,
                    messages = pending,
                    userId = user.id,
                    token = user.token,
                    userName = null,
                ).collect { result ->
                    result.onSuccess {
                        messageQueue.remove { m -> m.id in pending.map { s -> s.id } }
                    }
                }
            }
        }
        messageQueue.cancelTimer()
        // Reset overlay states to prevent stale state issues
        resetOverlayStates()
        super.onCleared()
    }

    private fun startMessageQueueTimer() {
        messageQueue.cancelTimer()
        messageQueue.startTimer { messages ->
            viewModelScope.launch {
                sendMessage(messages = messages, onSuccess = {
                    messageQueue.remove { m -> m.id in messages.map { s -> s.id } }
                })
            }
        }
    }

    private suspend fun sendMessage(
        userName: String? = null,
        messages: List<Message>,
        onSuccess: () -> Unit = {}
    ) {
        val messagesToSend =
            messages.filter {
                it.hiddenState !in listOf(
                    MessageState.Sending,
                    MessageState.Sent
                ) && !it.isAcknowledged
            }

        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        if (messagesToSend.isEmpty()) {
            return
        }

        _userCoinsCount.value = _userCoinsCount.value?.dec()?.coerceAtLeast(0)

        messageQueue.mark(state = MessageState.Sending)

        updateMessagesStateJob?.cancel()
        updateMessagesStateJob = viewModelScope.launch {
            delay(Random.nextLong(32L, 120L))
            updateConversationMessagesState(
                messagesToSend,
                MessageState.Sending
            )
            delay(Random.nextLong(500L, 1500L))
            updateConversationMessagesState(
                messagesToSend,
                MessageState.Sent
            )
            if (_characterStatus.value == CharacterStatus.Online) {
                delay(Random.nextLong(800L, 2000L))
                updateConversationMessagesState(
                    messagesToSend,
                    MessageState.Seen
                )
                delay(Random.nextLong(1200L, 3000L))
                setTypingMode(messages.any { m -> m.role == MessageRole.Assistant })
            }
        }

        executeFlowResultUseCase(
            useCase = {
                sendMessageUseCase(
                    characterId = aiCharacterId,
                    messages = messagesToSend.filter { elt -> elt.hiddenState !in setOf(MessageState.Sent) && !elt.isAcknowledged },
                    userId = user.id,
                    token = user.token,
                    userName = userName,
                )
            },
            onSuccess = {
                onSuccess()
                Log.d("ConversationViewModel", "onSuccess: message sent")
                messageQueue.mark(state = MessageState.Sent)
            },
            onFailure = {
                updateMessagesStateJob?.cancel()
                updateConversationMessagesState(messagesToSend, MessageState.Failed)
                messageQueue.mark(state = MessageState.Failed)
                _userCoinsCount.value = _userCoinsCount.value?.inc()
                if (it is UserNameNotFoundException) {
                    requestName()
                    return@executeFlowResultUseCase
                }
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }

    private suspend fun updateUserCoinsCount() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        executeFlowResultUseCase(
            useCase = {
                getAiTokensStateUseCase(
                    userId = user.id,
                )
            },
            onSuccess = {
                Log.d("ConversationViewModel", "onSuccess: message sent")
                _userCoinsCount.value = it.messagesCount
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }

    internal fun onClickSendButton() {
        if (_editTextMessage.value.isBlank()) {
            return
        }
        val message = transformTextToMessageUseCase(
            text = _editTextMessage.value,
        )

        // text to message
        _editTextMessage.value = ""

        // add message to conversation
        addMessageToConversationUseCase(message)

        // add message in queue
        messageQueue.add(message)
        messageQueue.cancelTimer()
        messageQueue.startTimer { messages ->
            viewModelScope.launch {
                sendMessage(messages = messages, onSuccess = {
                    messageQueue.remove { m -> m.id in messages.map { s -> s.id } }
                })
            }
        }
    }

    private companion object {
        const val MAX_WEBSOCKET_RECONNECT_ATTEMPTS = 3
    }
}