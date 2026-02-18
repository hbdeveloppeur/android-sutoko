package com.purpletear.aiconversation.presentation.screens.conversation.viewmodels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.data.exception.UserNameNotFoundException
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.repository.MessageQueue
import com.purpletear.aiconversation.domain.repository.MicrophoneRepository
import com.purpletear.aiconversation.domain.usecase.AddMessageToConversationUseCase
import com.purpletear.aiconversation.domain.usecase.SendMessageUseCase
import com.purpletear.aiconversation.domain.usecase.TransformFileToVoiceMessageUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.states.RecordingState
import com.purpletear.core.permission.PermissionChecker
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.EditTextPopUp
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceRecordViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val permissionChecker: PermissionChecker,
    private val microphoneRepository: MicrophoneRepository,
    private val messageQueue: MessageQueue,
    private val sendMessageUseCase: SendMessageUseCase,
    private val transformFileToVoiceMessageUseCase: TransformFileToVoiceMessageUseCase,
    private val addMessageToConversationUseCase: AddMessageToConversationUseCase,
    private val popUpInteractionUseCase: GetPopUpInteractionUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val customer: Customer,
) : ViewModel() {

    private var aiCharacterId: Int = 1

    private val _microphonePermissionRequired = MutableLiveData<Unit>()
    val microphonePermissionRequired: LiveData<Unit> = _microphonePermissionRequired

    private var updateMessagesStateJob: Job? = null

    private var timerJob: Job? = null

    private var _counter: MutableState<Int> = mutableIntStateOf(0)
    val counter: MutableState<Int> = _counter

    private var _isRecording: MutableState<Boolean> = mutableStateOf(false)
    val isRecording: MutableState<Boolean> = _isRecording

    init {
        aiCharacterId = savedStateHandle.get<Int>("character_id") ?: 1
    }

    internal fun onRecordingAction(state: RecordingState) {
        when (state) {
            RecordingState.StartRecording -> {
                startRecording()
            }

            is RecordingState.StopRecording -> {
                stopRecording(state.isCanceled)
            }
        }
    }

    private fun isUserConnected(): Boolean {
        return customer.isUserConnected()
    }

    // TODO
    private fun onUserNotConnected() {

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

        messageQueue.mark(state = MessageState.Sending)

        updateMessagesStateJob?.cancel()

        executeFlowResultUseCase(
            useCase = {
                sendMessageUseCase(
                    characterId = aiCharacterId,
                    messages = messagesToSend,
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
            // Failure
            Log.d("ConversationViewModel", "on Failure: ${it.message}")
        })
    }

    private fun startMessageQueueTimer() {
        messageQueue.cancelTimer()
        messageQueue.startTimer { messages ->
            sendMessage(messages = messages, onSuccess = {
                messageQueue.remove { m -> m.id in messages.map { s -> s.id } }
            })
        }
    }

    private fun stopRecording(isCanceled: Boolean) {
        val cancellation = isCanceled || _counter.value < 2
        _isRecording.value = false
        stopTimer()

        viewModelScope.launch(Dispatchers.IO) {
            if (!isUserConnected()) {
                microphoneRepository.stopRecording()
                onUserNotConnected()
                return@launch
            }

            if (microphoneRepository.isRecording()) {
                microphoneRepository.stopRecording()
                if (cancellation) {
                    microphoneRepository.clearRecordingDir()
                    startMessageQueueTimer()
                    return@launch
                }

                val file = microphoneRepository.getRecordingFile()
                val message = transformFileToVoiceMessageUseCase(file)
                addMessageToConversationUseCase(message)
                messageQueue.add(message)
                messageQueue.cancelTimer()
            }
            startMessageQueueTimer()
        }
    }


    private fun startRecording() {
        if (microphoneRepository.isRecording() || isRecording.value) {
            return
        }

        if (!permissionChecker.hasMicrophonePermission()) {
            _microphonePermissionRequired.value = Unit
            return
        }

        _isRecording.value = true
        stopTimer()
        messageQueue.cancelTimer()
        startTimer()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                microphoneRepository.startRecording()
            } catch (e: Exception) {
                _isRecording.value = false
                Log.e("AudioRecord", "Error starting recording", e)
            }
        }
    }


    private fun startTimer() {
        timerJob?.cancel()
        _counter.value = 0
        timerJob = viewModelScope.launch {
            while (_counter.value < 30) {
                delay(1000L)
                _counter.value += 1
            }

            stopRecording(false)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }
}