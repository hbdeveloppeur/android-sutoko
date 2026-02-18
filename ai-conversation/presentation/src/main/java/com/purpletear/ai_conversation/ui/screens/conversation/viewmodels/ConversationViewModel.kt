package com.purpletear.ai_conversation.ui.screens.conversation.viewmodels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sutokosharedelements.utils.UiText
import com.purpletear.ai_conversation.data.exception.NotEnoughCoinsException
import com.purpletear.ai_conversation.data.exception.UserNameNotFoundException
import com.purpletear.ai_conversation.domain.enums.CharacterStatus
import com.purpletear.ai_conversation.domain.enums.ConversationMode
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.model.messages.Conversation
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageImage
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.ai_conversation.domain.repository.MessageQueue
import com.purpletear.ai_conversation.domain.repository.WebSocketDataSource
import com.purpletear.ai_conversation.domain.sealed.WebSocketMessage
import com.purpletear.ai_conversation.domain.usecase.AddMessageToConversationUseCase
import com.purpletear.ai_conversation.domain.usecase.ClearConversationRepositoryUseCase
import com.purpletear.ai_conversation.domain.usecase.DescribeMediaUseCase
import com.purpletear.ai_conversation.domain.usecase.GetAvatarAndBannerPairUseCase
import com.purpletear.ai_conversation.domain.usecase.GetCharacterStatusUseCase
import com.purpletear.ai_conversation.domain.usecase.GetConversationMessagesUseCase
import com.purpletear.ai_conversation.domain.usecase.GetConversationSettingsUseCase
import com.purpletear.ai_conversation.domain.usecase.MakeStoryChoiceUseCase
import com.purpletear.ai_conversation.domain.usecase.RestartConversationUseCase
import com.purpletear.ai_conversation.domain.usecase.SaveForFineTuningUseCase
import com.purpletear.ai_conversation.domain.usecase.SelectMessageChoice
import com.purpletear.ai_conversation.domain.usecase.SendMessageMediaUseCase
import com.purpletear.ai_conversation.domain.usecase.SendMessageUseCase
import com.purpletear.ai_conversation.domain.usecase.TransformTextToUserMessageUseCase
import com.purpletear.ai_conversation.domain.usecase.TransformUrlToMessageImageUseCase
import com.purpletear.ai_conversation.domain.usecase.UpdateConversationMessagesState
import com.purpletear.ai_conversation.domain.usecase.UpdateDeviceTokenUseCase
import com.purpletear.ai_conversation.domain.usecase.UpdateMessageToConversationUseCase
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.common.utils.conversationListTransformer
import com.purpletear.ai_conversation.ui.model.UIMessage
import com.purpletear.ai_conversation.ui.sealed.AlertState
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.shop.domain.usecase.GetUserAccountStateUseCase
import com.purpletear.sutoko.permission.domain.sealed.Permission
import com.purpletear.sutoko.permission.domain.usecase.AskPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.EditTextPopUp
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val getUserAccountStateUseCase: GetUserAccountStateUseCase,
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
    private val customer: Customer,
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

    var messages = mutableStateListOf<UIMessage>()

    init {
        clearConversationRepositoryUseCase()
        aiCharacterId = savedStateHandle.get<Int>("character_id") ?: 1
        getConversationSettings()
        streamMessages()
        getCharacterStatus()
        bindToWebSocket()
        requestNotificationPermission()
        setUserDeviceToken()
    }

    fun onResume() {
        updateUserCoinsCount()
    }

    private fun setUserDeviceToken() {
        if (!customer.isUserConnected()) {
            return
        }
        executeFlowResultUseCase({
            updateDeviceTokenUseCase(customer.getUserId(), customer.getUserToken())
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
                sendMessage(
                    userName = (interaction.event as PopUpUserInteraction.ConfirmText).text,
                    messages = messagesToInsert.value,
                    onSuccess = {
                        messageQueue.remove { m -> m.id in messagesToInsert.value.map { s -> s.id } }
                    })
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

    internal fun userIsModerator(): Boolean {
        return customer.user.uid == "8be954c7a18f4e7cba9c"
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

    private var _isLoadingSavingForFineTuning: MutableState<Boolean> = mutableStateOf(false)
    val isLoadingSavingForFineTuning: State<Boolean> get() = _isLoadingSavingForFineTuning


    internal fun onClickSaveForFineTuning() {
        _isLoadingSavingForFineTuning.value = true
        executeFlowResultUseCase(
            useCase = { saveForFineTuningUseCase(customer.user.uid!!) },
            onSuccess = {
                _isLoadingSavingForFineTuning.value = false
            },
            onFailure = {
                _isLoadingSavingForFineTuning.value = false
                Log.d("ConversationViewModel", "on Failure: ${it.message}")
            }
        )
    }

    private fun getConversationSettings() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        _isLoading.value = true
        executeFlowUseCase(
            useCase = {
                getConversationSettingsUseCase(customer.user.uid!!, aiCharacterId)
            },
            onStream = { conversation ->
                _conversationSettings.value = conversation
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }

    private fun streamMessages() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        executeFlowUseCase(
            useCase = {
                getConversationMessagesUseCase(
                    customer.user.uid!!, aiCharacterId, customer.user.token!!,
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
                restartConversation()
            }
        }
    }

    private fun reset() {
        messageQueue.clear()
        _conversationSettings.value =
            _conversationSettings.value?.copy(isBlocked = false, mode = ConversationMode.Sms)
    }

    private fun restartConversation() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        reset()
        _isLoading.value = true
        executeFlowResultUseCase(
            useCase = { restartConversationUseCase(customer.user.uid!!, aiCharacterId) },
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

    private fun getCharacterStatus() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        executeFlowResultUseCase(
            useCase = { getCharacterStatusUseCase(customer.user.uid!!, aiCharacterId) },
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
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        if (messageStoryChoiceGroup.isConsumed) {
            return
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
            useCase = { makeStoryChoiceUseCase(customer.user.uid!!, messageStoryChoice) },
            onSuccess = {

            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
                // TODO :  Failure
            }
        )
    }

    // TODO
    private fun onConversationSettingsLoaded(settings: Conversation) {
        _conversationSettings.value = settings
        if (settings.isBlocked) {
            blockConversation()
        }
    }

    private fun onUserNotConnected() {
        // TODO("show error message")
    }

    private fun onAuthenticateFailure() {
        // TODO("show error message")
    }

    fun bindNavigationChanges(savedStateHandle: SavedStateHandle?) {
        val imageRequestSerialId: String? = savedStateHandle?.get("imageRequestSerialId")
        imageRequestSerialId?.let {
            savedStateHandle["imageRequestSerialId"] = null
            insertImageInConversation(it)
        }
    }

    private fun insertImageInConversation(imageRequestSerialId: String) {
        if (messages.isEmpty()) {
            toastService(R.string.ai_conversation_error_insert_media_conversation_empty)
            return
        }
        if (!customer.isUserConnected()) {
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
                            userId = customer.user.uid!!,
                            token = customer.user.token!!,
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
                        loadMediaDescription(message, media)
                    }
                }
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
                // Failure.
            }
        )
    }

    private fun loadMediaDescription(message: MessageImage, media: Media) {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        _loadingDescriptionMessageId.value = message.id
        executeFlowResultUseCase(useCase = {
            describeMediaUseCase(
                mediaId = media.id,
                userId = customer.user.uid!!,
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


    private fun bindToWebSocket() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            webSocketDataSource.connect(
                uid = customer.user.uid!!,
                token = customer.user.token!!
            ).collect { message ->
                withContext(Dispatchers.Main) {
                    handleWebSocketMessage(message)
                }
            }
        }
    }

    private fun onWebSocketError() {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            delay(1000L)
            if (webSocketDataSource.isConnected.not()) {
                bindToWebSocket()
            }
        }
    }

    private fun acknowledgeMessages(ids: List<String>) {
        messageQueue.acknowledge(ids)
    }

    private fun handleWebSocketMessage(it: WebSocketMessage) {
        when (it) {

            is WebSocketMessage.MessagesAck -> {
                acknowledgeMessages(it.serialIds)
            }

            is WebSocketMessage.ErrorCode -> {
                handleError(it.exception)
            }

            WebSocketMessage.AuthenticateFailure -> onAuthenticateFailure()
            WebSocketMessage.AuthenticateSuccess -> {
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

                if (customer.isUserConnected()) {
                    this.webSocketDataSource.sendPong(customer.user.uid!!, customer.user.token!!)
                } else {
                    onUserNotConnected()
                }
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


    override fun onCleared() {
        if (messageQueue.isNotEmpty()) {
            val messagesToInsert = messageQueue.messages
            sendMessage(messages = messagesToInsert.value, onSuccess = {
                messageQueue.remove { m -> m.id in messagesToInsert.value.map { s -> s.id } }
            })
        }
        messageQueue.cancelTimer()
        super.onCleared()
    }

    private fun startMessageQueueTimer() {
        messageQueue.cancelTimer()
        messageQueue.startTimer { messages ->
            sendMessage(messages = messages, onSuccess = {
                messageQueue.remove { m -> m.id in messages.map { s -> s.id } }
            })
        }
    }

    private fun sendMessage(
        userName: String? = null,
        messages: List<Message>,
        onSuccess: () -> Unit
    ) {
        val messagesToSend =
            messages.filter {
                it.hiddenState !in listOf(
                    MessageState.Sending,
                    MessageState.Sent
                ) && !it.isAcknowledged
            }
        if (!customer.isUserConnected()) {
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
                    userId = customer.user.uid!!,
                    token = customer.user.token!!,
                    userName = userName,
                )
            },
            onSuccess = {
                onSuccess()
                Log.d("ConversationViewModel", "onSuccess: message sent")
                messageQueue.mark(state = MessageState.Sent)
            },
            onFailure = {
                messageQueue.mark(state = MessageState.Failed)
                if (it is UserNameNotFoundException) {
                    requestName()
                    return@executeFlowResultUseCase
                }
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }

    private fun updateUserCoinsCount() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }
        executeFlowResultUseCase(
            useCase = {
                getUserAccountStateUseCase(
                    userId = customer.user.uid!!,
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
            sendMessage(messages = messages, onSuccess = {

            })
        }
    }

}